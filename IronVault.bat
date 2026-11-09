@echo off
setlocal
cd /d "%~dp0"

set "PORTABLE=%~dp0dist\IronVault\IronVault.exe"

if not exist "%PORTABLE%" (
  echo Building IronVault for the first launch...
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1"
  if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
  )
)

if exist "%PORTABLE%" (
  rem Seed the portable demo file from the project copy only when it is missing.
  if not exist "%~dp0dist\IronVault\demo-vault.iron" if not exist "%~dp0dist\IronVault\vault.iron" (
    if exist "%~dp0demo-vault.iron" copy /y "%~dp0demo-vault.iron" "%~dp0dist\IronVault\demo-vault.iron" >nul
    if exist "%~dp0demo-vault.iron.key" copy /y "%~dp0demo-vault.iron.key" "%~dp0dist\IronVault\demo-vault.iron.key" >nul
  )
  start "" /D "%~dp0dist\IronVault" "%PORTABLE%"
  exit /b 0
)

rem Fallback: jpackage is unavailable, run the JAR with the per-user vault.
set "JAVAW=javaw.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
if not exist "%JAVAW%" if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\javaw.exe" set "JAVAW=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\javaw.exe"

if not exist "dist\IronVault.jar" (
  echo dist\IronVault.jar not found.
  pause
  exit /b 1
)

start "" "%JAVAW%" -jar "dist\IronVault.jar"
