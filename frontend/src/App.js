import React, { useState, useEffect } from 'react';
import './App.css';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000';
const DASHBOARD_TOKEN = new URLSearchParams(window.location.search).get('token') || '';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [stats, setStats] = useState(null);
  const [flags, setFlags] = useState([]);
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      const [statsRes, flagsRes, playersRes] = await Promise.all([
        fetch(`${API_URL}/stats?token=${DASHBOARD_TOKEN}`),
        fetch(`${API_URL}/flags?token=${DASHBOARD_TOKEN}`),
        fetch(`${API_URL}/players?token=${DASHBOARD_TOKEN}`)
      ]);

      if (statsRes.ok) setStats(await statsRes.json());
      if (flagsRes.ok) setFlags((await flagsRes.json()).flags || []);
      if (playersRes.ok) setPlayers(await playersRes.json().then(d => d.players || []));
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading SentinelAI...</div>;

  return (
    <div className="app">
      <header className="header">
        <h1>SentinelAI AntiCheat</h1>
        <nav>
          <button className={activeTab === 'dashboard' ? 'active' : ''} onClick={() => setActiveTab('dashboard')}>Dashboard</button>
          <button className={activeTab === 'flags' ? 'active' : ''} onClick={() => setActiveTab('flags')}>Flags</button>
          <button className={activeTab === 'players' ? 'active' : ''} onClick={() => setActiveTab('players')}>Players</button>
        </nav>
      </header>

      <main className="content">
        {activeTab === 'dashboard' && <Dashboard stats={stats} />}
        {activeTab === 'flags' && <FlagsList flags={flags} />}
        {activeTab === 'players' && <PlayersList players={players} />}
      </main>
    </div>
  );
}

function Dashboard({ stats }) {
  if (!stats) return <div>No data available</div>;

  return (
    <div className="dashboard">
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Flags</h3>
          <p className="stat-number">{stats.totalFlags}</p>
        </div>
        <div className="stat-card">
          <h3>Total Players</h3>
          <p className="stat-number">{stats.totalPlayers}</p>
        </div>
        <div className="stat-card">
          <h3>Flags Today</h3>
          <p className="stat-number">{stats.flagsToday}</p>
        </div>
      </div>

      <div className="chart-section">
        <h2>Top Detection Reasons</h2>
        <div className="reason-bars">
          {(stats.topReasons || []).map((r, i) => (
            <div key={i} className="reason-bar">
              <span className="reason-name">{r.reason}</span>
              <div className="bar-bg">
                <div className="bar-fill" style={{width: `${(r.count / stats.totalFlags) * 100}%`}}></div>
              </div>
              <span className="reason-count">{r.count}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function FlagsList({ flags }) {
  return (
    <div className="flags-list">
      <h2>Recent Flags ({flags.length})</h2>
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Player</th>
            <th>Reason</th>
            <th>VL</th>
          </tr>
        </thead>
        <tbody>
          {flags.map(f => (
            <tr key={f.id}>
              <td>{new Date(f.created_at).toLocaleString()}</td>
              <td>{f.player_name}</td>
              <td><span className="badge">{f.reason}</span></td>
              <td>{f.vl}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function PlayersList({ players }) {
  return (
    <div className="players-list">
      <h2>Flagged Players ({players.length})</h2>
      <table>
        <thead>
          <tr>
            <th>Player</th>
            <th>Total Flags</th>
            <th>Last Seen</th>
          </tr>
        </thead>
        <tbody>
          {players.map(p => (
            <tr key={p.id}>
              <td>{p.player_name}</td>
              <td><span className="badge danger">{p.total_flags}</span></td>
              <td>{new Date(p.last_seen).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
