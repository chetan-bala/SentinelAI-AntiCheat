#!/bin/bash
echo "================================================"
echo "  SentinelAI AntiCheat - Setup Script"
echo "================================================"
echo

# Check dependencies
command -v java >/dev/null 2>&1 || { echo "ERROR: Java 17+ required"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "ERROR: Maven required"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "ERROR: Node.js required"; exit 1; }
command -v npm >/dev/null 2>&1 || { echo "ERROR: npm required"; exit 1; }

echo "[1/5] Building Paper Plugin..."
cd plugin && mvn clean package -q
if [ $? -ne 0 ]; then echo "ERROR: Maven build failed!"; exit 1; fi
echo "SUCCESS: Plugin built"

echo "[2/5] Setting up Backend..."
cd ../backend && npm install --silent
echo "SUCCESS: Backend ready"

echo "[3/5] Setting up Frontend..."
cd ../frontend && npm install --silent
echo "SUCCESS: Frontend ready"

echo "[4/5] Initializing Git..."
cd ..
git init 2>/dev/null
git add . 2>/dev/null
git commit -m "Initial commit: SentinelAI AntiCheat v1.0.0" 2>/dev/null
echo "SUCCESS: Git initialized"

echo "[5/5] Creating environment files..."
cp backend/.env.example backend/.env 2>/dev/null
echo "SUCCESS: Environment files created"

echo
echo "================================================"
echo "  Setup Complete!"
echo "================================================"
echo
echo "Next Steps:"
echo "1. Create GitHub repo: https://github.com/new"
echo "2. Push: git remote add origin <URL> && git push -u origin main"
echo "3. Deploy backend: https://render.com/deploy?repo=YOUR_REPO"
echo "4. Deploy frontend: Import to https://vercel.com"
echo "5. Copy plugin: cp plugin/target/*.jar to your server's plugins/"
echo
