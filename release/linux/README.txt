# DreamGesture Linux AppImage

## This is a placeholder for the DreamGesture Linux AppImage.

## To build the real AppImage:

### Prerequisites:
- Ubuntu 20.04+ / Fedora 36+ / similar
- Rust 1.75+ (https://rustup.rs)
- Node.js 20+ (https://nodejs.org)
- System libraries: libgtk-3-dev, libwebkit2gtk-4.0-dev

### Build Steps:

1. Install system dependencies (Ubuntu/Debian):
   sudo apt-get install -y libgtk-3-dev libwebkit2gtk-4.0-dev libayatana-appindicator3-dev librsvg2-dev patchelf

2. Open terminal in the GestureShare-unified folder:
   cd GestureShare-unified/desktop/tauri-app

3. Install dependencies:
   npm install

4. Build the AppImage:
   npm run tauri build

5. Find the AppImage at:
   src-tauri/target/release/bundle/appimage/

### To run the AppImage:

1. Make it executable:
   chmod +x DreamGesture*.AppImage

2. Run it:
   ./DreamGesture*.AppImage

### System Requirements:
- Most Linux distributions
- Webcam
- WiFi connection

For more help, visit: https://github.com/dreamerdkuroshin/DreamGesture
