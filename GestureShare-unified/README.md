# GestureShare

<div align="center">

![GestureShare Logo](assets/icons/dreamgesture_logo.png)

### Throw files between phones and computers using hand gestures.

**No accounts. No pairing. No cloud. Just gesture.**

</div>

---

## Download & Install (Super Simple)

### Option 1: Download ZIP (Easiest)

1. **Go to the [Releases page](../../releases)**
2. **Download** `GestureShare-v1.0.0.zip` for your computer
3. **Extract the ZIP** (right-click → "Extract All" on Windows, double-click on Mac)
4. **Open the folder** and run the installer inside

### Option 2: Download Individual Files

| What You Have | What To Download | File To Click |
|---------------|------------------|---------------|
| 📱 Android phone/tablet | APK file | `GestureShare-v1.0.0-android.apk` |
| 🪟 Windows PC/laptop | MSI installer | `GestureShare-v1.0.0-windows.msi` |
| 🍎 Mac computer | DMG file | `GestureShare-v1.0.0-macos.dmg` |
| 🐧 Linux computer | AppImage | `GestureShare-v1.0.0-linux.AppImage` |

---

## How To Install (Step By Step)

### 📱 Android - Install APK

**What you get:** A file called `GestureShare-v1.0.0-android.apk`

**How to install:**

1. **Download** the APK file to your phone
2. **Open your file manager** app (called "Files" or "My Files")
3. **Find the APK** in your Downloads folder
4. **Tap on it**
5. If it says "Install blocked" or "Unknown sources":
   - Tap **Settings**
   - Turn ON **"Allow from this source"** or **"Unknown sources"**
   - Go back and tap the APK again
6. Tap **"Install"**
7. Wait for it to finish
8. Tap **"Open"**
9. When it asks for permissions, tap **"Allow"** for:
   - Camera (needed for gestures)
   - Storage (needed for files)
   - Location (needed to find other devices)

**Done!** Open the app and tap "Start Detection".

---

### 🪟 Windows - Install MSI

**What you get:** A file called `GestureShare-v1.0.0-windows.msi`

**How to install:**

1. **Download** the MSI file to your computer
2. **Open your Downloads folder** (press `Windows key + E`, then click Downloads)
3. **Double-click** `GestureShare-v1.0.0-windows.msi`
4. If Windows says "Windows protected your PC":
   - Click **"More info"**
   - Then click **"Run anyway"**
5. The installer window opens → Click **"Next"**
6. Check **"I agree"** → Click **"Next"**
7. Click **"Install"**
8. Wait for it to finish
9. Check **"Launch GestureShare"**
10. Click **"Finish"**

**Done!** GestureShare opens. Click "Start Detection".

**First time only:** Windows Firewall will ask for permission:
- Check **"Private networks"**
- Click **"Allow access"**

---

### 🍎 Mac - Install DMG

**What you get:** A file called `GestureShare-v1.0.0-macos.dmg`

**How to install:**

1. **Download** the DMG file to your Mac
2. **Open your Downloads folder** (click Downloads in Finder sidebar)
3. **Double-click** `GestureShare-v1.0.0-macos.dmg`
4. A new window opens showing the app
5. **Drag the GestureShare icon** onto the **Applications folder**
6. Wait for it to copy
7. **Close the DMG window**
8. **Open Applications** (click Applications in Finder sidebar)
9. **Double-click GestureShare**
10. If it says "Cannot be opened":
    - **Right-click** the app → Click **"Open"**
    - Or: Open **System Preferences** → **Security** → Click **"Open Anyway"**
11. Click **"Open"** in the popup

**Done!** GestureShare opens. Click "Start Detection".

---

### 🐧 Linux - Run AppImage

**What you get:** A file called `GestureShare-v1.0.0-linux.AppImage`

**How to install:**

1. **Download** the AppImage file to your computer
2. **Open your Downloads folder**
3. **Right-click** the AppImage file
4. Click **"Properties"**
5. Go to **"Permissions"** tab
6. Check the box **"Allow executing file as program"**
7. Click **"Close"**
8. **Double-click** the AppImage file
9. It opens and runs!

**Alternative way using terminal:**

```bash
# Go to Downloads
cd ~/Downloads

# Make it executable
chmod +x GestureShare-v1.0.0-linux.AppImage

# Run it
./GestureShare-v1.0.0-linux.AppImage
```

**Done!** GestureShare opens. Click "Start Detection".

**If it doesn't work**, you might need to install these first:
```bash
sudo apt-get install libgtk-3-0 libwebkit2gtk-4.0-37
```

---

## How To Use (After Installing)

### First Time Setup

1. **Install GestureShare on 2 devices** (any combination works)
2. **Connect both to the same WiFi**
3. **Open GestureShare on both devices**
4. **Click "Start Detection"** on both
5. **Wait 5 seconds** — devices find each other automatically

### Send a File

| Device | How To Send |
|--------|-------------|
| **Android** | Take a screenshot (Power + Volume Down). GestureShare detects it automatically. |
| **Windows** | Press `PrtScn` key OR select a file → right-click → Share via GestureShare |
| **Mac** | Press `Cmd + Shift + 3` for screenshot OR drag file to GestureShare window |
| **Linux** | Press `PrtScn` key OR select file in GestureShare |

**Then:** Point at the other device and make a **THROW** gesture 🤾

### Receive a File

When someone sends you a file:
- Your device will **vibrate** and **glow**
- Make a **GRAB** gesture ✊ to accept
- Or make a **THUMB DOWN** 👎 to reject

---

## Gestures You Can Use

| Gesture | What It Does | How To Do It |
|---------|-------------|--------------|
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

## Example: Phone to Laptop

```
You (Phone)                          Friend (Laptop)
     │                                    │
     │  1. Take screenshot                │
     │  2. Open GestureShare              │
     │  3. Click "Start Detection"        │
     │                                    │
     │              ───── Both on same WiFi ─────
     │                                    │
     │  4. Point phone at laptop          │
     │  5. Make THROW gesture 🤾          │
     │                                    │
     │  ──── Encrypted transfer ────▶     │
     │                                    │
     │                           6. Laptop glows
     │                           7. Make GRAB ✊
     │                           8. Screenshot appears!
```

---

## Troubleshooting

### "Devices not finding each other"
- Make sure both are on the **same WiFi**
- Make sure **Location permission** is granted
- Make sure **Start Detection** is clicked on both
- Try turning WiFi off and on again

### "Gesture not recognized"
- Make sure there's **good lighting**
- Keep your **whole hand in the camera view**
- Hold the gesture **steady for 1 second**
- Stay **30-80cm** from the camera

### "Windows says the file is unsafe"
- This is normal for new apps
- Click **"More info"** → **"Run anyway"**

### "Mac says app can't be opened"
- **Right-click** the app → **"Open"**
- Or go to **System Preferences** → **Security** → **"Open Anyway"**

### "Linux AppImage won't run"
- Right-click → **Properties** → **Permissions** → Check **"Allow executing"**
- Or run: `chmod +x GestureShare*.AppImage`

---

## System Requirements

| Platform | Minimum Version | Needs |
|----------|----------------|-------|
| Android | Android 12+ | Front camera, WiFi |
| Windows | Windows 10/11 (64-bit) | Webcam, WiFi |
| macOS | macOS 11+ (Big Sur) | Webcam, WiFi |
| Linux | Ubuntu 20.04+ / Fedora 36+ | Webcam, WiFi |

---

## Security

- ✅ **End-to-end encrypted** (AES-256-GCM)
- ✅ **No cloud** — files go directly between devices
- ✅ **No accounts** — no sign-up needed
- ✅ **No logs** — nothing stored after transfer
- ✅ **No internet required** — works on local WiFi only

---

## For Developers

Want to build from source? See the full documentation:

- [Building from Source](docs/BUILD.md)
- [Protocol Specification](docs/PROTOCOL.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)

### Quick Build Commands

```bash
# Clone repository
git clone https://github.com/gestureshare/unified.git
cd unified

# Build everything
./scripts/build-all.sh

# Or build specific platform:
./scripts/build-android.sh    # Android APK
./scripts/build-desktop.sh    # Windows/Mac/Linux
```

---

## License

Apache 2.0 — Free to use, modify, and distribute.

---

<div align="center">

**GestureShare** — Throw files with a wave of your hand 🤚

[Download ZIP](../../releases) · [Report Bug](../../issues) · [Source Code](.)

</div>
