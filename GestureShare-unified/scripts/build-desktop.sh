#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
TAURI_DIR="$PROJECT_DIR/desktop/tauri-app"

echo "=== Building GestureShare Desktop ==="

echo "Step 1: Installing npm dependencies..."
cd "$TAURI_DIR"
npm install

echo "Step 2: Building Tauri app..."
npm run tauri build

echo "=== Build Complete ==="
echo "Output: $TAURI_DIR/src-tauri/target/release/bundle/"
