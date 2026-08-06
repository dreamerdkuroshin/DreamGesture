# DreamGesture Windows MSI Installer

## This is a placeholder for the DreamGesture Windows MSI.

## To build the real MSI:

### Prerequisites:
- Windows 10/11 (64-bit)
- Rust 1.75+ (https://rustup.rs)
- Node.js 20+ (https://nodejs.org)
- Visual Studio Build Tools 2022
- Windows SDK

### Build Steps:

1. Open terminal in the GestureShare-unified folder:
   cd GestureShare-unified/desktop/tauri-app

2. Install dependencies:
   npm install

3. Build the MSI:
   npm run tauri build

4. Find the MSI at:
   src-tauri/target/release/bundle/msi/

### To install the MSI:

1. Double-click the MSI file
2. If Windows SmartScreen appears: Click "More info" -> "Run anyway"
3. Follow the installation wizard
4. Launch from Start Menu

### System Requirements:
- Windows 10/11 (64-bit)
- Webcam
- WiFi connection

For more help, visit: https://github.com/dreamerdkuroshin/DreamGesture
