#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PROTOCOL_DIR="$PROJECT_DIR/core/protocol-rs"
ANDROID_DIR="$PROJECT_DIR/android"

echo "=== Building GestureShare Android ==="

echo "Step 1: Building Rust JNI libraries..."
cd "$PROTOCOL_DIR"
cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86_64 \
    -t x86 \
    -o "$ANDROID_DIR/app/src/main/jniLibs" \
    build --release

echo "Step 2: Building Android APK..."
cd "$ANDROID_DIR"
./gradlew :app:assembleRelease

echo "=== Build Complete ==="
echo "APK: $ANDROID_DIR/app/build/outputs/apk/release/app-release.apk"
