@echo off
title Book Management - database setup
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-db.ps1"
pause
