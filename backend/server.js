// SentinelAI Backend API
// Node.js + Express + SQLite
// Free hosting ready for Render.com

const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const Database = require('better-sqlite3');
const { v4: uuidv4 } = require('uuid');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8000;
const API_KEY = process.env.API_KEY || 'sentinel-free-key-12345';
const DASHBOARD_TOKEN = process.env.DASHBOARD_TOKEN || uuidv4();

// Initialize SQLite database
const db = new Database('sentinelai.db');

// Create tables
db.exec(`
  CREATE TABLE IF NOT EXISTS flags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT NOT NULL,
    player_uuid TEXT,
    reason TEXT NOT NULL,
    vl INTEGER DEFAULT 0,
    clip_data TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT UNIQUE,
    player_name TEXT,
    total_flags INTEGER DEFAULT 0,
    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS clips (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    flag_id INTEGER,
    clip_data TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flag_id) REFERENCES flags(id)
  );
`);

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
});
app.use(limiter);

// API Key validation middleware
const validateApiKey = (req, res, next) => {
  const apiKey = req.headers['x-api-key'];
  if (!apiKey || apiKey !== API_KEY) {
    return res.status(401).json({ error: 'Invalid API key' });
  }
  next();
};

// Dashboard token middleware (optional security)
const validateDashboardToken = (req, res, next) => {
  const token = req.query.token || req.headers['x-dashboard-token'];
  if (!token || token !== DASHBOARD_TOKEN) {
    return res.status(401).json({ error: 'Invalid dashboard token' });
  }
  next();
};

// ==================== API ENDPOINTS ====================

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', version: '1.0.0' });
});

// POST /flag - Receive flag from Minecraft plugin
app.post('/flag', validateApiKey, (req, res) => {
  try {
    const { playerName, reason, vl, clipData } = req.body;

    if (!playerName || !reason) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Insert flag
    const flagStmt = db.prepare(`
      INSERT INTO flags (player_name, player_uuid, reason, vl, clip_data)
      VALUES (?, ?, ?, ?, ?)
    `);
    const flagResult = flagStmt.run(playerName, req.body.uuid || null, reason, vl || 0, clipData || null);

    // Update or insert player
    const playerStmt = db.prepare(`
      INSERT INTO players (uuid, player_name, total_flags, last_seen)
      VALUES (?, ?, 1, CURRENT_TIMESTAMP)
      ON CONFLICT(uuid) DO UPDATE SET
        total_flags = total_flags + 1,
        last_seen = CURRENT_TIMESTAMP,
        player_name = excluded.player_name
    `);
    playerStmt.run(req.body.uuid || playerName, playerName);

    // Save clip if provided
    if (clipData) {
      const clipStmt = db.prepare(`
        INSERT INTO clips (flag_id, clip_data)
        VALUES (?, ?)
      `);
      clipStmt.run(flagResult.lastInsertRowid, clipData);
    }

    res.status(201).json({ success: true, flagId: flagResult.lastInsertRowid });
  } catch (error) {
    console.error('Error saving flag:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /flags - Get all flags (with pagination)
app.get('/flags', validateDashboardToken, (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 50;
    const offset = (page - 1) * limit;

    const flags = db.prepare(`
      SELECT * FROM flags
      ORDER BY created_at DESC
      LIMIT ? OFFSET ?
    `).all(limit, offset);

    const total = db.prepare('SELECT COUNT(*) as count FROM flags').get().count;

    res.json({
      flags,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Error fetching flags:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /players - Get all players
app.get('/players', validateDashboardToken, (req, res) => {
  try {
    const players = db.prepare('SELECT * FROM players ORDER BY total_flags DESC').all();
    res.json({ players });
  } catch (error) {
    console.error('Error fetching players:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /clips/:id - Get specific clip
app.get('/clips/:id', validateDashboardToken, (req, res) => {
  try {
    const clip = db.prepare('SELECT * FROM clips WHERE id = ?').get(req.params.id);
    if (!clip) {
      return res.status(404).json({ error: 'Clip not found' });
    }
    res.json({ clip });
  } catch (error) {
    console.error('Error fetching clip:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GET /stats - Get statistics
app.get('/stats', validateDashboardToken, (req, res) => {
  try {
    const stats = {
      totalFlags: db.prepare('SELECT COUNT(*) as count FROM flags').get().count,
      totalPlayers: db.prepare('SELECT COUNT(*) as count FROM players').get().count,
      flagsToday: db.prepare(`SELECT COUNT(*) as count FROM flags WHERE DATE(created_at) = DATE('now')`).get().count,
      topReasons: db.prepare(`
        SELECT reason, COUNT(*) as count
        FROM flags
        GROUP BY reason
        ORDER BY count DESC
        LIMIT 5
      `).all()
    };
    res.json(stats);
  } catch (error) {
    console.error('Error fetching stats:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Dashboard info (includes the token URL)
app.get('/dashboard-info', (req, res) => {
  res.json({
    dashboardUrl: `${req.protocol}://${req.get('host')}/dashboard?token=${DASHBOARD_TOKEN}`,
    message: 'Share this URL to access the dashboard (no login required)'
  });
});

// Start server
app.listen(PORT, () => {
  console.log('========== SentinelAI Backend ==========');
  console.log(`Server running on port ${PORT}`);
  console.log(`Dashboard URL: http://localhost:${PORT}/dashboard?token=${DASHBOARD_TOKEN}`);
  console.log('=======================================');
});

// Graceful shutdown
process.on('SIGINT', () => {
  db.close();
  process.exit(0);
});
