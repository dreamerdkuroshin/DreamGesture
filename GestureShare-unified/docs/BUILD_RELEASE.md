# Building Release Packages

This guide explains how to build all platform packages for distribution.

## Prerequisites

### All Platforms
- Git
- Rust 1.75+ (`rustup.rs`)
- Node.js 20+ (`nodejs.org`)

### Android
- Android Studio Hedgehog+
- JDK 17
- Android SDK 34
- NDK 25.2.9519653
- `cargo install cargo-ndk`

### Windows
- Visual Studio Build Tools 2022
- Windows SDK
- WebView2 Runtime

### macOS
- Xcode Command Line Tools (`xcode-select --install`)
- macOS 11+

### Linux
```bash
sudo apt-get install -y libgtk-3-dev libwebkit2gtk-4.0-dev \
    libayatana-appindicator3-dev librsvg2-dev patchelf
```

## Quick Build (All Platforms)

```bash
./scripts/build-release.sh
```

## Platform-Specific Builds

### Android APK

```bash
# 1. Build Rust JNI libraries
cd core/protocol-rs
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 \
    -o ../android/app/src/main/jniLibs build --release

# 2. Copy icons
for dir in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
    cp assets/icons/logo_$dir.png android/app/src/main/res/mipmap-$dir/ic_launcher.png
    cp assets/icons/logo_$dir.png android/app/src/main/res/mipmap-$dir/ic_launcher_round.png
done

# 3. Build APK
cd android
./gradlew :app:assembleRelease

# Output: android/app/build/outputs/apk/release/app-release.apk
```

### Windows MSI

```bash
cd desktop/tauri-app
npm install
npm run tauri build

# Output: src-tauri/target/release/bundle/msi/*.msi
```

### macOS DMG

```bash
cd desktop/tauri-app
npm install
npm run tauri build

# Output: src-tauri/target/release/bundle/dmg/*.dmg
```

### Linux AppImage + DEB

```bash
cd desktop/tauri-app
npm install
npm run tauri build

# Output:
#   src-tauri/target/release/bundle/appimage/*.AppImage
#   src-tauri/target/release/bundle/deb/*.deb
```

## Icon Setup

The app icon is `assets/icons/dreamgesture_logo.png` (512x512).

Platform-specific icons are auto-generated:
- Android: `android/app/src/main/res/mipmap-*/ic_launcher.png`
- Windows: `desktop/tauri-app/src-tauri/icons/icon.ico`
- macOS: `desktop/tauri-app/src-tauri/icons/icon.icns`
- Linux: `desktop/tauri-app/src-tauri/icons/icon.png`

## CI/CD (GitHub Actions)

Push a tag to trigger automatic builds:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This builds all platforms and attaches binaries to the GitHub Release.

## Distribution

After building, copy outputs to `dist/`:

```
dist/
├── android/
│   ├── GestureShare-v1.0.0-android.apk
│   ├── ic_launcher.png
│   └── README.txt
├── windows/
│   ├── GestureShare-v1.0.0-windows.msi
│   ├── icon.png
│   └── README.txt
├── macos/
│   ├── GestureShare-v1.0.0-macos.dmg
│   ├── icon.png
│   └── README.txt
├── linux/
│   ├── GestureShare-v1.0.0-linux.AppImage
│   ├── GestureShare-v1.0.0-linux.deb
│   ├── icon.png
│   └── README.txt
└── index.html
```

## Code Signing

### Android
```bash
# Generate key
keytool -genkey -v -keystore gestureshare.jks -keyalg RSA -keysize 2048 -validity 10000

# Sign APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore gestureshare.jks app-release.apk gestureshare
```

### Windows
Use `signtool` with a code signing certificate.

### macOS
Use `codesign` with an Apple Developer certificate.

### Linux
AppImages can be signed with GPG.
