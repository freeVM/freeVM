@echo off
rem $Id: subversion-config-install.cmd 665 2007-02-06 04:23:19Z vlads $


copy "%~dp0subversion_config" "%APPDATA%\Subversion\config"
if errorlevel 1 (
    echo Error calling copy
    pause
    exit /b 1
)
