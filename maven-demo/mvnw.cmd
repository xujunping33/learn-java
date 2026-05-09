@echo off
setlocal

set BASE_DIR=%~dp0
set WRAPPER_PROPS=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties

for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
  if "%%A"=="distributionUrl" set DIST_URL=%%B
)

if "%DIST_URL%"=="" (
  echo distributionUrl not found in %WRAPPER_PROPS%
  exit /b 1
)

set M2_DIR=%USERPROFILE%\.m2
set WRAPPER_DIR=%M2_DIR%\wrapper
set ZIP_PATH=%WRAPPER_DIR%\maven.zip
set DEST_DIR=%WRAPPER_DIR%\apache-maven

if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

if not exist "%ZIP_PATH%" (
  echo Downloading Maven from %DIST_URL%
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'" || exit /b 1
)

if not exist "%DEST_DIR%" (
  mkdir "%DEST_DIR%"
  powershell -Command "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%DEST_DIR%'" || exit /b 1
)

for /r "%DEST_DIR%" %%F in (mvn.cmd) do (
  set MVN_BIN=%%F
  goto :run
)

echo Maven binary not found after unzip.
exit /b 1

:run
call "%MVN_BIN%" -f "%BASE_DIR%\pom.xml" %*

