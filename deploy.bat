@echo off
echo ================================================
echo   SentinelAI AntiCheat - Deploy Script
echo ================================================
echo.

echo [1/4] Building Paper Plugin...
cd plugin
call mvn clean package -q
if errorlevel 1 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)
echo SUCCESS: Plugin built at plugin\target\SentinelAI-AntiCheat-1.0.0.jar
echo.

echo [2/4] Installing Backend Dependencies...
cd ..\backend
call npm install --silent
echo SUCCESS: Backend dependencies installed
echo.

echo [3/4] Installing Frontend Dependencies...
cd ..\frontend
call npm install --silent
echo SUCCESS: Frontend dependencies installed
echo.

echo [4/4] Creating GitHub Repository...
cd ..
git init
git add .
git commit -m "Initial commit: SentinelAI AntiCheat v1.0.0"
echo.
echo ================================================
echo   Deployment Files Ready!
echo ================================================
echo.
echo Next Steps:
echo 1. Create GitHub repo: https://github.com/new
echo 2. Push code: git remote add origin ^<YOUR_REPO_URL^> ^&^& git push -u origin main
echo 3. Deploy backend: https://render.com/deploy?repo=YOUR_REPO
echo 4. Deploy frontend: Import to https://vercel.com
echo.
pause
