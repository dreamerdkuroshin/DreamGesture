# Build Instructions

## Prerequisites

### All Platforms
- Git
- Rust 1.75+ (install via [rustup](https://rustup.rs/))
- Node.js 20+ (for desktop app)

### Android
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- NDK 25.x
- `cargo-ndk`: `cargo install cargo-ndk`

### Windows
- Visual Studio Build Tools 2022 (C++ workload)
- Windows SDK 10.0.19041.0+
- WebView2 Runtime (or use Tauri runtime)

### macOS
- Xcode Command Line Tools: `xcode-select --install`
- macOS 11.0+ (Big Sur)

### Linux
```bash
sudo apt-get install -y \
    libgtk-3-dev \
    libwebkit2gtk-4.0-dev \
    libayatana-appindicator3-dev \
    librsvg2-dev \
    patchelf \
    libx11-dev \
    libvulkan-dev
```

## Building the Rust Core

```bash
cd core/protocol-rs
cargo build --cargo build --release
cargo test --release
```

### Generate C Headers (for Android JNI)

```bash
cargo install cbindgen
cbindgen --lang c --output include/gesture_protocol.h
```

## Building the Android APK

### 1. Build Rust JNI Libraries

```bash
cd core/protocol-rs
cargo ndk \
    -t arm64-v8a \
    -t armeabi-v7a \
    -t x86_64 \
    -t x86 \
    -o ../android/app/src/main/jniLibs \
    build --release
```

### 2. Build APK

```bash
cd android
./gradlew :app:assembleDebug    # Debug APK
./gradlew :app:assembleRelease  # Release APK
```

### 3. Output

- Debug: `android/app/build/outputs/apk/debug/app-debug.apk`
- Release: `android/app/build/outputs/apk/release/app-release.apk`

### 4. Install

```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

## Building the Desktop App

### 1. Install Dependencies

```bash
cd desktop/tauri-app
npm install
```

### 2. Development Mode

```bash
npm run tauri dev
```

### 3. Build Release

```bash
npm run tauri build
```

### 4. Output

| Platform | Output Path | Format |
|----------|-------------|--------|
| Windows | `src-tauri/target/release/bundle/msi/*.msi` | MSI installer |
| Windows | `src-tauri/target/release/bundle/nsis/*.exe` | NSIS installer |
| macOS | `src-tauri/target/release/bundle/dmg/*.dmg` | Disk image |
| macOS | `src-tauri/target/release/bundle/macos/*.app` | App bundle |
| Linux | `src-tauri/target/release/bundle/appimage/*.AppImage` | AppImage |
| Linux | `src-tauri/target/release/bundle/deb/*.deb` | Debian package |

## Platform-Specific Build Commands

### Windows (from Windows)

```powershell
cd desktop\tauri-app
npm install
npm run tauri build
```

### macOS (from macOS)

```bash
cd desktop/tauri-app
npm install
npm run tauri build
```

### Linux (from Linux)

```bash
cd desktop/tauri-app
npm install
npm run tauri build
```

### Cross-Compilation

#### Windows → Linux (experimental)
```bash
cargo install cross
cross build --release --target x86_64-unknown-linux-gnu
```

#### Linux → Windows
```bash
rustup target add x86_64-pc-windows-gnu
cargo build --release --target x86_64-pc-windows-gnu
```

## Downloading AI Models

```bash
./scripts/download_models.sh
```

This downloads:
- `hand_landmarker.task` (MediaPipe Hands)
- `gesture_classifier.tflite` (Custom gesture classifier)

Place models in:
- Android: `android/app/src/main/assets/models/`
- Desktop: `desktop/tauri-app/src-tauri/models/`

## Running Tests

### Rust Tests
```bash
cd core/protocol-rs
cargo test --release
```

### Android Tests
```bash
cd android
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Desktop Tests
```bash
cd desktop/tauri-app/src-tauri
cargo test --release
```

## CI/CD

GitHub Actions automatically builds all platforms on push to `main`:

1. Rust core library (all targets)
2. Android APK (debug + release)
3. Windows MSI
4. macOS DMG
5. Linux AppImage + DEB

See `.github/workflows/build-all.yml` for details.

## Troubleshooting

### Android NDK not found
```bash
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/25.2.9519653
```

### Tauri build fails on Linux
```bash
sudo apt-get install -y libgtk-3-dev libwebkit2gtk-4.0-dev
```

### MediaPipe model not found
Run `./scripts/download_models.sh` and verify files exist in `assets/models/`.

### Camera permission denied
- Android: Check `AndroidManifest.xml` for camera permission
- Desktop: Grant camera access in OS settings
