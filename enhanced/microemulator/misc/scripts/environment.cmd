@echo off
rem @version $Revision: 1379 $ ($Author: vlads $)  $Date: 2007-10-12 22:00:43 -0400 (Fri, 12 Oct 2007) $

call %~dp0version.cmd

rem set JAVA_HOME=D:\jdk1.5.0
rem set PATH=%JAVA_HOME%\bin;%PATH%

set MAVEN2_REPO=%HOMEDRIVE%\%HOMEPATH%\.m2\repository

set BUILD_HOME=%~dp0
for /f %%i in ("%DEFAULT_BUILD_HOME%..\..") do @set BUILD_HOME=%%~fi

echo BUILD_HOME=[%BUILD_HOME%]

