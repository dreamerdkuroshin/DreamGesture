# DreamGesture (GestureShare)

<div align="center">

![DreamGesture Logo](dreamgesture_logo.png)

### Throw files between phones and computers using hand gestures.

**No accounts. No pairing. No cloud. Just gesture.**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS%20%7C%20Linux-brightgreen)]()
[![Status](https://img.shields.io/badge/Status-Active-success.svg)]()

</div>

---

## 📥 Download & Install

### Quick Download

| Platform | Download | File Type | Requirements |
|----------|----------|-----------|--------------|
| 📱 **Android** | [Download APK](https://github.com/dreamerdkuroshin/DreamGesture/releases/latest/download/DreamGesture-android.apk) | APK | Android 12+ |
| 🪟 **Windows** | [Download MSI](https://github.com/dreamerdkuroshin/DreamGesture/releases/latest/download/DreamGesture-windows.msi) | MSI | Windows 10/11 |
| 🍎 **macOS** | [Download DMG](https://github.com/dreamerdkuroshin/DreamGesture/releases/latest/download/DreamGesture-macos.dmg) | DMG | macOS 11+ |
| 🐧 **Linux** | [Download AppImage](https://github.com/dreamerdkuroshin/DreamGesture/releases/latest/download/DreamGesture-linux.AppImage) | AppImage | Most distros |

> **Don't see your platform?** Go to [Releases](https://github.com/dreamerdkuroshin/DreamGesture/releases) for all available files.

---

## 🚀 How To Install (Step By Step)

### 📱 Android

1. **Download** the APK file to your phone
2. **Open** your file manager (called "Files" or "My Files")
3. **Find** the APK in Downloads
4. **Tap** on it
5. If "Install blocked": Tap **Settings** → Turn ON **"Allow from this source"**
6. Tap **"Install"**
7. Tap **"Open"**
8. **Allow** Camera, Storage, and Location permissions

**Done!** Open the app and tap "Start Detection".

---

### 🪟 Windows

1. **Download** the MSI file
2. **Double-click** the MSI file
3. If "Windows protected your PC": Click **"More info"** → **"Run anyway"**
4. Click **"Next"** → Check **"I agree"** → Click **"Install"**
5. Click **"Finish"**

**First time only:** Allow camera in Settings → Privacy → Camera.

**Done!** Open from Start Menu.

---

### 🍎 macOS

1. **Download** the DMG file
2. **Double-click** the DMG file
3. **Drag** DreamGesture to Applications folder
4. **Open** Applications → Double-click DreamGesture
5. If "Cannot be opened": **Right-click** → **"Open"**

**Done!** Click "Start Detection".

---

### 🐧 Linux

1. **Download** the AppImage file
2. **Right-click** → **Properties** → **Permissions** → Check **"Allow executing"**
3. **Double-click** to run

**Or via terminal:**
```bash
chmod +x DreamGesture*.AppImage
./DreamGesture*.AppImage
```

**Done!** Click "Start Detection".

---

## 🎮 How To Use

### First Time Setup

1. **Install** DreamGesture on 2+ devices
2. **Connect** all devices to the same WiFi
3. **Open** DreamGesture on each device
4. **Click "Start Detection"** on each
5. **Wait 5 seconds** — devices find each other!

### Send a File

| Device | Action |
|--------|--------|
| **Android** | Take screenshot (Power + Vol Down) — auto detected |
| **Windows** | Press `PrtScn` or select file → Share |
| **Mac** | Press `Cmd+Shift+3` or drag file to app |
| **Linux** | Press `PrtScn` or select file in app |

**Then:** Point at target device and make a **THROW** gesture 🤾

### Receive a File

When someone sends you a file:
- Device **vibrates** and **glows**
- Make **GRAB** gesture ✊ to accept
- Or **THUMB DOWN** 👎 to reject

---

## 🤚 Gestures

| Gesture | Action | How To Do It |
|---------|--------|--------------|
| 🤾 Throw | **Send file** | Push hand forward toward target |
| ✊ Grab | **Receive file** | Close hand, pull toward you |
| 👈 Swipe Left | **Cancel** | Move hand left |
| 👉 Swipe Right | **Send** | Move hand right |
| 👌 Pinch | **Copy** | Thumb + index touch |
| 🖐️ Open Palm | **Preview** | Show palm to camera |
| 👋 Wave | **Find devices** | Wave hand side to side |
| 👍 Thumb Up | **Accept** | Thumb up |
| 👎 Thumb Down | **Reject** | Thumb down |
| ✊ Fist | **Pause** | Make a fist |

---

## 🔒 Security

- ✅ **End-to-end encrypted** (AES-256-GCM)
- ✅ **No cloud** — direct device-to-device
- ✅ **No accounts** — no sign-up needed
- ✅ **No logs** — nothing stored after transfer
- ✅ **Offline** — works on local WiFi only

---

## 📋 System Requirements

| Platform | Minimum | Needs |
|----------|---------|-------|
| Android | Android 12+ | Front camera, WiFi |
| Windows | Windows 10/11 (64-bit) | Webcam, WiFi |
| macOS | macOS 11+ (Big Sur) | Webcam, WiFi |
| Linux | Ubuntu 20.04+ / Fedora 36+ | Webcam, WiFi |

---

## ❓ Troubleshooting

**Devices not finding each other?**
- Same WiFi network? ✓
- Location permission granted? ✓
- "Start Detection" clicked on both? ✓

**Gesture not recognized?**
- Good lighting? ✓
- Whole hand in camera view? ✓
- Hold gesture steady 1 second? ✓

**Windows says file unsafe?**
- Click "More info" → "Run anyway"

**Mac says can't open?**
- Right-click app → "Open"

---

## 📁 Project Structure

This repository contains two projects:

1. **[GestureShare](GestureShare/)** — Native Android app (Kotlin, Jetpack Compose, CameraX, MediaPipe)
2. **[GestureShare-unified](GestureShare-unified/)** — Cross-platform engine (Rust + Tauri for Windows/macOS/Linux + Android)

---

## 🛠️ Build from Source

### Android
```bash
cd GestureShare
./gradlew assembleRelease
```

### Desktop (Windows/macOS/Linux)
```bash
cd GestureShare-unified
cd core/protocol-rs && cargo build --release
cd ../../desktop/tauri-app
npm install
npm run tauri build
```

See [docs/BUILD.md](docs/BUILD.md) for detailed instructions.

---

## 📄 Documentation

- [Download & Install](#-download--install) (above)
- [Architecture](docs/ARCHITECTURE.md)
- [Protocol Specification](docs/PROTOCOL.md)
- [Security Audit](docs/SECURITY.md)
- [Gesture Reference](docs/GESTURES.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)

---

## 📜 License

Apache 2.0 — Free to use, modify, and distribute. See [LICENSE](LICENSE).

---

<div align="center">

**DreamGesture** — Throw files with a wave of your hand 🤚

[Download](https://github.com/dreamerdkuroshin/DreamGesture/releases) · [Report Bug](https://github.com/dreamerdkuroshin/DreamGesture/issues) · [Source Code](.)

</div>
