# Clip Viewer - How to Replay Player Movements

## Overview
The clip system records 30 seconds of player data (positions, rotations, actions) as JSON, which can be replayed in the web dashboard.

## Clip Data Structure
```json
{
  "playerName": "Steve",
  "uuid": "xxx-xxx-xxx",
  "timestamp": 1234567890,
  "positions": [
    {"x": 100.5, "y": 64.0, "z": 200.3, "yaw": 90.0, "pitch": 0.0, "timestamp": 1234567890}
  ],
  "actions": [
    {"action": "ATTACK", "timestamp": 1234567890}
  ]
}
```

## Viewing Clips

### In Dashboard
1. Go to Flags page
2. Click on a flag with clip data
3. Clip viewer shows:
   - 2D top-down map with player path
   - Timeline slider (0-30 seconds)
   - Player markers at each position
   - Action icons (attack, interact)

### React Component (Add to frontend)
```jsx
function ClipViewer({ clipData }) {
  const [currentTime, setCurrentTime] = useState(0);
  const positions = JSON.parse(clipData).positions;

  return (
    <div className="clip-viewer">
      <div className="map-container">
        {positions.map((pos, i) => (
          <div
            key={i}
            className="player-marker"
            style={{
              left: `${pos.x % 100}px`,
              top: `${pos.z % 100}px`,
              opacity: i === currentTime ? 1 : 0.3
            }}
          />
        ))}
      </div>
      <input
        type="range"
        min="0"
        max={positions.length - 1}
        value={currentTime}
        onChange={e => setCurrentTime(parseInt(e.target.value))}
      />
    </div>
  );
}
```

## 3D Replay (Optional Enhancement)
Use Three.js for 3D visualization:
```bash
cd frontend
npm install three
```

## Export Options
- Download as JSON
- Export to video (using canvas recording)
- Share clip link
