# GestureShare

Production-quality Android application that replicates and improves Huawei's AI hand gesture sharing system.

Take a screenshot → Make a gesture → Share to nearby devices. No taps. No QR codes. No manual pairing.

## How It Works

```
Screenshot Taken → AI Camera Detects Gesture → Direction Estimation →
Target Selection → Encrypted Transfer → Animation Complete
```

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────────┐
│           Presentation (Compose UI)          │
├─────────────────────────────────────────────┤
│               Domain (Use Cases)             │
├─────────────────────────────────────────────┤
│                 Data (Repos)                 │
└─────────────────────────────────────────────┘
```

### Module Structure

```
GestureShare/
├── app/                    # Application entry point
├── core/
│   ├── common/             # Shared utilities, Result wrapper
│   ├── domain/             # Entities, use cases, interfaces
│   ├── data/               # Repository implementations
│   ├── di/                 # Hilt dependency injection
│   ├── ui/                 # Compose theme, shared components
│   ├── security/           # AES-256-GCM, ECDH, secure memory
│   └── analytics/          # Performance monitoring
└── feature/
    ├── screenshot-listener # Background screenshot detection
    ├── camera/             # CameraX + power adaptive
    ├── gesture-vision/     # MediaPipe + TFLite hand recognition
    ├── gesture-engine/     # Kalman filter, optical flow, sequence
    ├── nearby-discovery/   # WiFi Direct, BLE, UDP, mDNS
    ├── transfer-engine/    # Encrypted chunked transfer
    ├── ui-animation/       # 60FPS physics animations
    └── main-screen/        # Main UI with MVVM
```

## AI Pipeline

```
Front Camera (CameraX)
       │
       ▼
MediaPipe Hands (GPU/CPU fallback)
       │
       ▼
21 Hand Landmarks (x, y, z, visibility)
       │
       ▼
Gesture Recognition (Rule-based + TFLite)
       │
       ▼
Kalman Filter + Optical Flow Tracking
       │
       ▼
Confidence Filter (>95%)
       │
       ▼
Gesture Engine (Single/Double/Sequence)
       │
       ▼
Direction Estimation → Target Selection
       │
       ▼
Transfer Engine (AES-256-GCM + ECDH)
       │
       ▼
Receive + Gallery Animation
```

## Features

### Supported Gestures

| Gesture | Action |
|---------|--------|
| Palm | Open share menu |
| Grab + Throw | Send screenshot to pointed device |
| Point | Select target device |
| Push | Confirm transfer |
| Pull | Cancel transfer |
| Swipe Left/Right | Navigate between targets |
| Thumb Up | Confirm |
| Peace Sign | Quick share to last device |
| Wave | Discovery mode |

### Security

- **AES-256-GCM** encryption for all transfers
- **ECDH** key exchange with ephemeral session keys
- **No cloud** - fully offline peer-to-peer
- **No persistent storage** of screenshots post-transfer
- **No logs** of sensitive data
- Hardware-backed keystore for key storage

### Performance Targets

| Metric | Target |
|--------|--------|
| RAM | < 250MB |
| CPU | < 15% |
| Battery | < 3%/hr |
| Latency | < 300ms |
| Recognition | < 20ms |
| Animation | 60 FPS |

## Building

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- NDK 25.x (for MediaPipe)

### Build Commands

```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew ktlintCheck detekt

# Install debug APK
./gradlew :app:installDebug
```

### Setup

1. Clone repository
2. Open in Android Studio
3. Sync Gradle
4. Download MediaPipe hand landmarker model to `app/src/main/assets/models/`
5. Run on device (API 31+)

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Camera | CameraX |
| AI | MediaPipe Hands, TensorFlow Lite |
| Crypto | AES-256-GCM, ECDH (secp384r1) |
| Networking | WiFi Direct, BLE, UDP, mDNS |
| DI | Hilt |
| Async | Coroutines + Flow |
| Background | WorkManager |
| Testing | JUnit, MockK, Turbine, Truth |

## License

Apache 2.0 - See LICENSE file for details.
