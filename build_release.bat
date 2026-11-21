@echo off
powershell -ExecutionPolicy Bypass -File "%~dp0increment_build.ps1"
echo.
echo Build number incremented. Now build your artifacts in IntelliJ.
pause
