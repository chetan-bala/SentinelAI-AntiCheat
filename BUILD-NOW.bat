@echo off
echo ================================================
echo   SentinelAI Auto-Builder
echo ================================================
echo.

echo [1/4] Downloading Maven...
powershell -Command "Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"
powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%CD%\tools' -Force"

echo [2/4] Downloading Paper API...
powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/io/papermc/paper/paper-api/1.20.4-R0.1-SNAPSHOT/paper-api-1.20.4-R0.1-SNAPSHOT.jar' -OutFile 'plugin\paper-api.jar' -ErrorAction SilentlyContinue"

echo [3/4] Compiling plugin...
cd plugin
set PATH=%PATH%;%CD%\..\tools\apache-maven-3.9.6\bin
call mvnw.cmd clean package -DskipTests

echo [4/4] Done!
echo.
echo Your JAR is at:
echo %CD%\target\SentinelAI-AntiCheat-1.0.0.jar
echo.
pause
