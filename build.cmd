@echo off
REM Full build: compile + tests (JUnit) + static analysis (Checkstyle) + package.
REM chcp 65001 makes Maven messages readable in a Windows console.
chcp 65001 >nul
cd /d "%~dp0"
call mvn -B clean verify
