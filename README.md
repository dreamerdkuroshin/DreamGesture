# DreamGesture (GestureShare)

<div align="center">

<img src="dreamgesture_logo.png" width="120" alt="DreamGesture Logo">

### Throw files between phones and computers using hand gestures.

**No accounts. No pairing. No cloud. Just gesture.**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS%20%7C%20Linux-brightgreen)]()

</div>

---

## 📥 Download & Install

### Quick Download

| Platform | Download | Requirements |
|----------|----------|--------------|
| 📱 **Android** | [Download APK](https://github.com/dreamerdkuroshin/DreamGesture/raw/main/release/android/README.txt) | Android 12+ |
| 🪟 **Windows** | [Download MSI](https://github.com/dreamerdkuroshin/DreamGesture/raw/main/release/windows/README.txt) | Windows 10/11 |
| 🍎 **macOS** | [Download DMG](https://github.com/dreamerdkuroshin/DreamGesture/raw/main/release/macos/README.txt) | macOS 11+ |
| 🐧 **Linux** | [Download AppImage](https://github.com/dreamerdkuroshin/DreamGesture/raw/main/release/linux/README.txt) | Most distros |

> **Auto-built via GitHub Actions** — CI/CD builds automatically on push.

---

## 🚀 Quick Install

### Android
1. Download APK
2. Tap to install (enable "Unknown sources" if needed)
3. Grant Camera + Storage + Location permissions
4. Open app → Tap "Start Detection"

### Windows
1. Download MSI
2. Double-click → "More info" → "Run anyway" (if SmartScreen)
3. Follow installer
4. Open from Start Menu

### macOS
1. Download DMG
2. Double-click → Drag to Applications
3. Right-click → Open (first time)

### Linux
1. Download AppImage
2. Right-click → Properties → Permissions → "Allow executing"
3. Double-click to run

---

## 🎮 How To Use

1. **Install** on 2+ devices (same WiFi)
2. **Open** DreamGesture on each device
3. **Click "Start Detection"** on each
4. **Take screenshot** or select file
5. **Make THROW gesture** toward target device
6. **File transfers automatically!**

---

## 🤚 Gestures

| Gesture | Action |
|---------|--------|
| 🤾 Throw | Send file |
| ✊ Grab | Receive file |
| 👈 Swipe Left | Cancel |
| 👉 Swipe Right | Send |
| 👋 Wave | Find devices |
| 👍 Thumb Up | Accept |
| 👎 Thumb Down | Reject |

---

## 🔒 Security

- ✅ AES-256-GCM encryption
- ✅ ECDH P-384 key exchange
- ✅ No cloud, no server, no logs
- ✅ All processing on-device

---

## 📁 Repository Structure

```
DreamGesture/
├── GestureShare/              # Android app (Kotlin + Jetpack Compose)
│   ├── app/                   # Main Android application
│   ├── core/                  # Domain, Data, UI, Security modules
│   ├── feature/               # Camera, Gesture, Transfer modules
│   └── docs/                  # Android documentation
├── GestureShare-unified/      # Cross-platform engine
│   ├── core/protocol-rs/      # Shared Rust protocol library
│   ├── android/               # Android with Rust FFI
│   ├── desktop/               # Tauri app (Windows/macOS/Linux)
│   └── docs/                  # Protocol & architecture docs
├── dreamgesture_logo.png      # App icon
└── gen_icons.py               # Icon generation script
```

---

## 🛠️ Build from Source

### Android
```bash
cd GestureShare
./gradlew assembleRelease
```

### Desktop (Windows/macOS/Linux)
```bash
cd GestureShare-unified/core/protocol-rs
cargo build --release
cd ../../desktop/tauri-app
npm install
npm run tauri build
```

---

## 📄 Documentation

- [Download Guide](GestureShare-unified/DOWNLOADS.md)
- [Architecture](GestureShare-unified/docs/ARCHITECTURE.md)
- [Protocol Spec](GestureShare-unified/docs/PROTOCOL.md)
- [Security Audit](GestureShare-unified/docs/SECURITY.md)
- [Gesture Reference](GestureShare-unified/docs/GESTURES.md)
- [Developer Guide](GestureShare/docs/DEVELOPER_GUIDE.md)

---

## 📜 License

Apache 2.0 — See [LICENSE](LICENSE)

---

<div align="center">

**DreamGesture** — Throw files with a wave of your hand 🤚

[Downloads](https://github.com/dreamerdkuroshin/DreamGesture/releases) · [Issues](https://github.com/dreamerdkuroshin/DreamGesture/issues)

</div>
