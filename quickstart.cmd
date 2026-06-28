@echo off
REM Quick start script for OpenSwift development (Windows)

setlocal enabledelayedexpansion

echo === OpenSwift Dev Setup ===
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java 17+ not found. Install from:
    echo   https://www.oracle.com/java/technologies/javase-downloads.html
    exit /b 1
)

for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| find "version"') do (
    set JAVA_VERSION=%%a
    set JAVA_VERSION=!JAVA_VERSION:"=!
    for /f "delims=." %%b in ("!JAVA_VERSION!") do set JAVA_MAJOR=%%b
)

if %JAVA_MAJOR% lss 17 (
    echo ERROR: Java 17+ required ^(found %JAVA_MAJOR%^)
    exit /b 1
)
echo ✓ Java %JAVA_MAJOR% found
echo.

REM Check Android SDK
if not defined ANDROID_HOME (
    echo ERROR: ANDROID_HOME not set. Install Android Studio:
    echo   https://developer.android.com/studio
    echo.
    echo Then add to system environment variables:
    echo   ANDROID_HOME=C:\Users\^<YourUsername^>\AppData\Local\Android\sdk
    exit /b 1
)
echo ✓ Android SDK found at %ANDROID_HOME%
echo.

REM Build debug APK
echo Building OpenSwift (debug)...
call gradlew.bat assembleDebug

echo.
echo ✓ Build complete!
echo.
echo Next steps:
echo   1. Connect Android device ^(or start emulator^)
echo   2. adb install app\build\outputs\apk\debug\app-debug.apk
echo   3. Open Settings ^> Languages ^& input ^> On-screen keyboard
echo   4. Enable OpenSwift
echo   5. Open any text field and start typing!
echo.
echo For release build:
echo   gradlew.bat assembleRelease
echo.
echo For more info, see README.md
