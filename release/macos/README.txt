# DreamGesture macOS DMG

## This is a placeholder for the DreamGesture macOS DMG.

## To build the real DMG:

### Prerequisites:
- macOS 11+ (Big Sur)
- Xcode Command Line Tools: xcode-select --install
- Rust 1.75+ (https://rustup.rs)
- Node.js 20+ (https://nodejs.org)

### Build Steps:

1. Open terminal in the GestureShare-unified folder:
   cd GestureShare-unified/desktop/tauri-app

2. Install dependencies:
   npm install

3. Build the DMG:
   npm run tauri build

4. Find the DMG at:
   src-tauri/target/release/bundle/dmg/

### To install the DMG:

1. Double-click the DMG file
2. Drag DreamGesture to Applications folder
3. Right-click -> Open (first time only)
4. Grant Camera permission when prompted

### System Requirements:
- macOS 11+ (Big Sur)
- Intel or Apple Silicon
- Webcam
- WiFi connection

For more help, visit: https://github.com/dreamerdkuroshin/DreamGesture
