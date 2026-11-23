@echo off
REM Launch the application with a UTF-8 console (Windows code page 65001).
REM Without chcp the console uses OEM code page 866 and Cyrillic is unreadable.
chcp 65001 >nul
cd /d "%~dp0"
java -jar target\ticket-system-1.0.0-shaded.jar
