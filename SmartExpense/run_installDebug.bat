@echo off
cd /d "%~dp0"
echo Running gradlew installDebug...
call gradlew.bat installDebug
pause
