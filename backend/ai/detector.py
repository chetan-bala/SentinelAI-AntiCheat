# SentinelAI - Simple AI Detection System
# Uses anomaly detection to adjust thresholds and reduce false positives
# Optional: Can be disabled in config

import json
import numpy as np
from datetime import datetime
import sqlite3

class AIDetector:
    def __init__(self, db_path='sentinelai.db'):
        self.db_path = db_path
        self.thresholds = {
            'speed': 0.35,
            'fly': 0.5,
            'killaura_yaw': 30,
            'reach': 3.5,
            'autoclicker_cps': 20
        }
        self.learning_rate = 0.01
        self.enabled = True

    def analyze_player_behavior(self, player_uuid):
        """Analyze player behavior patterns and adjust thresholds"""
        if not self.enabled:
            return self.thresholds

        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        # Get player's flag history
        cursor.execute("""
            SELECT reason, COUNT(*) as count, AVG(vl) as avg_vl
            FROM flags
            WHERE player_uuid = ?
            GROUP BY reason
        """, (player_uuid,))

        flags = cursor.fetchall()

        # Get recent flags for trend analysis
        cursor.execute("""
            SELECT reason, vl, created_at
            FROM flags
            WHERE created_at > datetime('now', '-7 days')
            ORDER BY created_at DESC
            LIMIT 100
        """)

        recent_flags = cursor.fetchall()
        conn.close()

        # Adjust thresholds based on patterns
        self._adjust_thresholds(flags, recent_flags)

        return self.thresholds

    def _adjust_thresholds(self, flags, recent_flags):
        """Dynamically adjust detection thresholds to reduce false positives"""
        if not recent_flags:
            return

        # Calculate flag frequency
        flag_counts = {}
        for reason, count, avg_vl in flags:
            flag_counts[reason] = count

        total_flags = sum(flag_counts.values())

        # If too many flags for a check, increase threshold (reduce sensitivity)
        for reason, count in flag_counts.items():
            if count > 50:  # High flag count
                if reason in self.thresholds:
                    self.thresholds[reason] *= (1 + self.learning_rate)
            elif count < 5:  # Very few flags
                if reason in self.thresholds:
                    self.thresholds[reason] *= (1 - self.learning_rate * 0.5)

    def detect_anomaly(self, player_data):
        """Simple anomaly detection using statistical methods"""
        if not player_data or len(player_data) < 10:
            return False, 0.0

        # Extract features
        features = []
        for record in player_data[-30:]:  # Last 30 records
            if 'speed' in record:
                features.append(record['speed'])
            if 'cps' in record:
                features.append(record['cps'])

        if not features:
            return False, 0.0

        # Simple z-score anomaly detection
        mean = np.mean(features)
        std = np.std(features)

        if std == 0:
            return False, 0.0

        latest = features[-1]
        z_score = abs((latest - mean) / std)

        is_anomaly = z_score > 2.5
        confidence = min(z_score / 5.0, 1.0)

        return is_anomaly, confidence

    def save_thresholds(self):
        """Save current thresholds to database"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        cursor.execute("""
            CREATE TABLE IF NOT EXISTS ai_thresholds (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                check_name TEXT,
                threshold REAL,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)

        for check, threshold in self.thresholds.items():
            cursor.execute("""
                INSERT OR REPLACE INTO ai_thresholds (check_name, threshold)
                VALUES (?, ?)
            """, (check, threshold))

        conn.commit()
        conn.close()

    def load_thresholds(self):
        """Load thresholds from database"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()

            cursor.execute("SELECT check_name, threshold FROM ai_thresholds")
            rows = cursor.fetchall()

            for check_name, threshold in rows:
                self.thresholds[check_name] = threshold

            conn.close()
        except:
            pass  # Use defaults if can't load

    def get_stats(self):
        """Get AI detection statistics"""
        return {
            'enabled': self.enabled,
            'thresholds': self.thresholds,
            'learning_rate': self.learning_rate,
            'model_type': 'anomaly_detection'
        }
