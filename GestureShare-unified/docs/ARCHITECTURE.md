# GestureShare Unified - Cross-Platform Architecture

## System Overview

GestureShare enables gesture-based file sharing between any combination of Android phones, Windows/Linux/macOS desktops and laptops using a shared Rust protocol core.

```
┌─────────────────────────────────────────────────────────────────┐
│                    Shared Protocol Core (Rust)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────────┐  │
│   Crypto    │  Discovery │  Transfer  │  Gesture         │  │
│  AES/ECDH  │  mDNS/UDP  │  QUIC/UDP  │  Classification  │  │
│  └──────────┘  └──────────┘  └──────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐
│   Android App    │ │  Desktop App    │ │      Future: iOS        │
│   (Kotlin)       │ │  (Tauri + Web)  │ │      (Swift)            │
│                  │ │                 │ │                         │
│ CameraX          │ │ Webcam          │ │ AVFoundation            │
│ MediaPipe        │ │ OpenCV/MediaPipe│ │ Vision Framework        │
│ Jetpack Compose  │ │ Svelte/React    │ │ SwiftUI                 │
│ Hilt DI          │ │ Rust Backend    │ │ CoreML                  │
│ ┌──────────────┐│ │ ┌─────────────┐ │ │                         │
│ │ JNI Bridge   ││ │ │ Direct FFI  │ │ │                         │
│ └──────────────┘│ │ └─────────────┘ │ │                         │
└─────────────────┘ └─────────────────┘ └─────────────────────────┘
```

## Module Structure

```
GestureShare-unified/
├── core/
│   └── protocol-rs/          # Shared Rust protocol library
│       ├── src/
│       │   ├── lib.rs        # Library exports
│       │   ├── crypto.rs     # AES-256-GCM, ECDH, SHA-256
│       │   ├── protocol.rs   # Wire format, message types
│       │   ├── discovery.rs  # mDNS, UDP broadcast, BLE
│       │   ├── transfer.rs   # Chunked encrypted transfer
│       │   ├── gesture.rs    # Gesture classification
│       │   └── direction.rs  # Direction estimation
│       ├── Cargo.toml
│       └── tests/
├── android/                   # Android application
│   └── app/
│       ├── src/main/
│       │   ├── java/com/geshtureshare/
│       │   │   ├── ffi/      # JNI bridge to Rust
│       │   │   ├── camera/   # CameraX integration
│       │   │   ├── vision/   # MediaPipe + TFLite
│       │   │   ├── gesture/  # Gesture engine
│       │   │   ├── nearby/   # WiFi Direct, BLE
│       │   │   ├── transfer/ # Transfer engine
│       │   │   ├── ui/       # Compose UI
│       │   │   └── main/     # Main screen + VM
│       │   └── jniLibs/      # Compiled Rust .so files
│       └── build.gradle.kts
├── desktop/                   # Desktop application
│   ├── tauri-app/            # Tauri frontend + config
│   │   ├── src/              # Web UI (HTML/CSS/JS)
│   │   ├── src-tauri/        # Tauri Rust backend
│   │   │   ├── src/
│   │   │   │   ├── main.rs
│   │   │   │   ├── commands.rs
│   │   │   │   ├── state.rs
│   │   │   │   └── gesture_service.rs
│   │   │   └── Cargo.toml
│   │   ├── package.json
│   │   └── tauri.conf.json
│   ├── gesture-engine-rs/    # Desktop gesture recognition
│   │   └── src/
│   │       ├── webcam.rs     # Cross-platform webcam
│   │       ├── landmarker.rs # Hand landmark detection
│   │       └── classifier.rs # Gesture classification
│   └── screen-capture-rs/    # Cross-platform screen capture
│       └── src/lib.rs
├── docs/                      # Documentation
│   ├── PROTOCOL.md
│   ├── ARCHITECTURE.md
│   ├── BUILD.md
│   ├── SECURITY.md
│   └── GESTURES.md
├── scripts/                   # Build scripts
│   ├── build-android.sh
│   ├── build-desktop.sh
│   └── download_models.sh
└── .github/workflows/         # CI/CD
    └── build-all.yml
```

## Data Flow

### Sending a File (Phone → Laptop)

```
1. User takes screenshot on phone
   ↓
2. ScreenshotListenerService detects via ContentObserver
   ↓
3. CameraX captures front camera frame
   ↓
4. MediaPipe detects 21 hand landmarks
   ↓
5. GestureRecognizer classifies gesture (e.g., "throw")
   ↓
6. GestureEngine validates confidence >95%
   ↓
7. DirectionEstimator determines target device
   ↓
8. DiscoveryService finds nearby devices
   ↓
9. Protocol establishes session (ECDH key exchange)
   ↓
10. TransferEngine encrypts and sends chunks
    ↓
11. Laptop receives, decrypts, verifies hash
    ↓
12. File saved to downloads folder
```

### Receiving a File (Laptop → Phone)

```
1. User selects file on laptop
   ↓
2. Webcam captures hand gesture
   ↓
3. Desktop gesture engine detects "throw"
   ↓
4. Protocol sends TransferOffer to phone
   ↓
5. Phone accepts (auto or gesture)
   ↓
6. Encrypted transfer begins
    ↓
7. Phone receives chunks, decrypts
    ↓
8. Integrity verified via SHA-256
    ↓
9. File saved to gallery
```

## Platform Communication Matrix

| From ↓ / To → | Android | Windows | macOS | Linux |
|---------------|---------|---------|-------|-------|
| **Android** | ✅ | ✅ | ✅ | ✅ |
| **Windows** | ✅ | ✅ | ✅ | ✅ |
| **macOS** | ✅ | ✅ | ✅ | ✅ |
| **Linux** | ✅ | ✅ | ✅ | ✅ |

All combinations use the same protocol over WiFi/LAN.

## Shared Rust Core

The `gesture_protocol` crate provides:

1. **Crypto**: AES-256-GCM encryption, ECDH key exchange, SHA-256 hashing
2. **Protocol**: Wire format, message types, serialization
3. **Discovery**: UDP broadcast, mDNS service discovery
4. **Transfer**: Chunked encrypted transfer with retry/resume
5. **Gesture**: Landmark-based gesture classification
6. **Direction**: 3D direction estimation for target selection

### FFI Bindings

- **Android**: JNI bridge via `cbindgen` → `.so` libraries
- **Desktop**: Direct Rust FFI (Tauri commands call Rust directly)
- **iOS** (future): C header → Swift bridging header

## Security Model

```
┌──────────────────────────────────────────────┐
│             Security Layers                    │
├──────────────────────────────────────────────┤
│ 1. ECDH P-384 Key Exchange (per session)     │
│ 2. AES-256-GCM Authenticated Encryption      │
│ 3. SHA-256 Integrity Verification            │
│ 4. Ephemeral Session Keys (destroyed after)  │
│ 5. No Cloud / No Server / Fully Offline      │
│ 6. No Persistent Storage of Transferred Data │
└──────────────────────────────────────────────┘
```

## Performance Targets

| Metric | Target |
|--------|--------|
| Gesture Recognition | < 20ms |
| End-to-end Latency | < 300ms |
| Transfer Throughput | > 40 Mbps (WiFi Direct) |
| Memory (Desktop) | < 200MB |
| Memory (Android) | < 250MB |
| CPU (Idle) | < 5% |
| CPU (Active) | < 15% |
| Battery (Android) | < 3%/hr |
