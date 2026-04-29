@echo off
setlocal

set MAVEN_WRAPPER_VERSION=3.2.0
set WRAPPER_URL="https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar"
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"

if not exist %WRAPPER_JAR% (
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"
)

java -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
