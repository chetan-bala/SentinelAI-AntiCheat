# SentinelAI AntiCheat

Modern AI-powered Minecraft anti-cheat system for Paper servers with web dashboard.

## Features

- **6 Detection Checks**: Speed, Fly, KillAura, Reach, NoSlow, AutoClicker
- **AI System**: Anomaly detection with dynamic threshold adjustment
- **Clip Recording**: 30-second buffer with JSON export
- **Web Dashboard**: React-based control panel (no login required)
- **Free Hosting**: Deploy on Render + Vercel

## Quick Start

### 1. Build Plugin
```bash
cd plugin
mvn clean package
```

### 2. Deploy Backend (Render)
- Push to GitHub
- Connect repo to Render.com
- Set environment variables (see DEPLOYMENT.md)

### 3. Deploy Frontend (Vercel)
- Import GitHub repo to Vercel
- Set root directory to `frontend`
- Add `REACT_APP_API_URL` environment variable

### 4. Install Plugin
- Copy .jar to `plugins/` folder
- Edit `config.yml` with your backend URL
- Run `/sentinel reload`

## Project Structure

```
SentinelAI-AntiCheat/
├── plugin/              # Paper plugin (Java/Maven)
├── backend/             # Node.js API (Express + SQLite)
├── frontend/            # React dashboard
├── ai/                  # Python AI system
└── docs/                # Documentation
```

## Configuration

Edit `plugins/SentinelAI/config.yml`:
```yaml
api:
  url: "https://your-backend.onrender.com"
  key: "your-secret-key"
ai:
  enabled: true
```

## Dashboard Access

Get your dashboard URL:
```bash
curl https://your-backend.onrender.com/dashboard-info
```

Share the tokenized URL - no passwords needed!

## License

MIT License - Free for everyone!
