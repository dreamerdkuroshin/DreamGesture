#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "========================================="
echo "  GestureShare Unified - Build All"
echo "========================================="

echo ""
echo "Step 1: Building Rust Protocol Core..."
cd "$PROJECT_DIR/core/protocol-rs"
cargo build --release
cargo test --release
echo "  Rust core built successfully."

echo ""
echo "Step 2: Building Android APK..."
if command -v cargo-ndk &> /dev/null; then
    cd "$PROJECT_DIR/core/protocol-rs"
    cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 \
        -o "$PROJECT_DIR/android/app/src/main/jniLibs" build --release
    cd "$PROJECT_DIR/android"
    ./gradlew :app:assembleRelease
    echo "  Android APK built successfully."
else
    echo "  WARNING: cargo-ndk not found. Skipping Android build."
    echo "  Install with: cargo install cargo-ndk"
fi

echo ""
echo "Step 3: Building Desktop App..."
cd "$PROJECT_DIR/desktop/tauri-app"
if [ ! -d "node_modules" ]; then
    npm install
fi
npm run tauri build
echo "  Desktop app built successfully."

echo ""
echo "========================================="
echo "  All builds complete!"
echo "========================================="
echo ""
echo "Outputs:"
echo "  Android: $PROJECT_DIR/android/app/build/outputs/apk/release/"
echo "  Desktop: $PROJECT_DIR/desktop/tauri-app/src-tauri/target/release/bundle/"
