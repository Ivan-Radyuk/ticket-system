@echo off
REM Static analysis only: mvn checkstyle:check
REM Returns a non-zero exit code if any Checkstyle rule is violated.
chcp 65001 >nul
cd /d "%~dp0"
call mvn -B checkstyle:check
