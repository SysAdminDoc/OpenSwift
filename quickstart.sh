#!/bin/bash
# Quick start script for OpenSwift development

set -e

echo "=== OpenSwift Dev Setup ==="
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17+ not found. Install from:"
    echo "  https://www.oracle.com/java/technologies/javase-downloads.html"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep 'version' | cut -d' ' -f3 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERROR: Java 17+ required (found $JAVA_VERSION)"
    exit 1
fi
echo "✓ Java $JAVA_VERSION found"
echo ""

# Check Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME not set. Install Android SDK:"
    echo "  https://developer.android.com/studio"
    echo ""
    echo "Then add to ~/.bashrc or ~/.zshrc:"
    echo "  export ANDROID_HOME=\$HOME/Library/Android/sdk"
    exit 1
fi
echo "✓ Android SDK found at $ANDROID_HOME"
echo ""

# Build debug APK
echo "Building OpenSwift (debug)..."
./gradlew assembleDebug

echo ""
echo "✓ Build complete!"
echo ""
echo "Next steps:"
echo "  1. Connect Android device (or start emulator)"
echo "  2. adb install app/build/outputs/apk/debug/app-debug.apk"
echo "  3. Open Settings > Languages & input > On-screen keyboard"
echo "  4. Enable OpenSwift"
echo "  5. Open any text field and start typing!"
echo ""
echo "For release build:"
echo "  ./gradlew assembleRelease"
echo ""
echo "For more info, see CONTRIBUTING.md"
