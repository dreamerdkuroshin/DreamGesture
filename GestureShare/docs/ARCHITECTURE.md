# GestureShare Architecture

## Overview

GestureShare follows Clean Architecture with MVVM pattern, modularized by feature.

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│   MainScreen │  Animation │  Camera   │  Screenshot          │ │
│   ViewModel  │  Composables│  Preview  │  Overlay             │ │
│  └─────┬────┘  └─────┬────┘  └────┬────┘  └────────┬─────────┘ │
│        │             │            │                  │            │
├────────┼─────────────┼────────────┼──────────────────┼────────────┤
│        │           Domain Layer     │                  │            │
│  ┌─────▼─────────────▼─────────────▼──────────────────▼─────────┐ │
│  │                     Use Cases                                 │ │
│  │  DetectGesture │ TransferScreenshot │ ProcessScreenshot      │ │
│  └──────────────────────────┬───────────────────────────────────┘ │
│                             │                                     │
│  ┌──────────────────────────▼───────────────────────────────────┐ │
│  │                   Repository Interfaces                        │ │
│  │  GestureRepository │ ScreenshotRepository │ DeviceRepository │ │
│  └──────────────────────────┬───────────────────────────────────┘ │
├─────────────────────────────┼─────────────────────────────────────┤
│                          Data Layer                               │
│  ┌──────────────────────────▼───────────────────────────────────┐ │
│  │               Repository Implementations                       │ │
│  │  GestureRepositoryImpl │ ScreenshotRepoImpl │ DeviceRepoImpl │ │
│  └──────────────────────────┬───────────────────────────────────┘ │
│                             │                                     │
│  ┌──────────┐  ┌───────────▼──┐  ┌──────────┐  ┌──────────────┐ │
│  │ MediaPipe│  │  MediaStore   │  │WiFi Dir. │  │ TFLite       │ │
│  │ Hands    │  │  Content Obs. │  │ BLE, mDNS│  │ Interpreter  │ │
│  └──────────┘  └──────────────┘  └──────────┘  └──────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## State Management

### Unidirectional Data Flow

```
User Action → ViewModel → Use Case → Repository → External System
                ▲                                         │
                └──────────── State Flow ─────────────────┘
```

### State Classes

```kotlin
data class MainUiState(
    val isServiceRunning: Boolean,
    val currentScreenshot: Screenshot?,
    val lastGesture: Gesture?,
    val nearbyDevices: List<NearbyDevice>,
    val transferState: TransferState,
    val selectedDevice: NearbyDevice?,
    val cameraActive: Boolean,
    val errorMessage: String?
)
```

## Data Flow

### Screenshot Detection Flow

```
MediaStore Change → ContentObserver → ScreenshotDetector → Repository → UI State
```

### Gesture Recognition Flow

```
Camera Frame → MediaPipe → Landmarks → GestureRecognizer → Kalman Filter →
Gesture Engine (>95% confidence) → Direction Estimation → Target Selection
```

### Transfer Flow

```
Gesture Trigger → Select Target → ECDH Key Exchange → Encrypt →
Chunk → Send → Verify Hash → Complete
```

## Module Dependencies

```
app
├── main-screen
│   ├── gesture-engine
│   │   └── domain
│   ├── nearby-discovery
│   │   └── domain
│   ├── transfer-engine
│   │   ├── security
│   │   └── domain
│   └── ui-animation
│       └── ui
├── screenshot-listener
│   ├── data
│   └── domain
├── camera
│   └── domain
├── gesture-vision
│   └── domain
├── core/ui
├── core/data
│   ├── domain
│   └── security
└── core/di
    ├── domain
    ├── data
    └── security
```

## Dependency Injection

All dependencies are managed via Hilt:

- `@HiltViewModel` for ViewModels
- `@Inject` constructor injection for repositories and use cases
- `@Module` + `@Provides` for framework dependencies
- `@Binds` for interface implementations
- `@Singleton` scope for app-wide singletons

## Sequence Diagram: Gesture-to-Transfer

```
User          Camera      Vision     Engine      Discovery    Transfer
 │               │           │          │           │            │
 │──gesture─────▶│           │          │           │            │
 │               │──frame──▶│          │           │            │
 │               │           │─detect──▶│           │            │
 │               │           │──gesture─▶│           │            │
 │               │           │           │─confirm──▶│            │
 │               │           │           │           │─select────▶│
 │               │           │           │           │            │─encrypt
 │               │           │           │           │            │──send──▶
 │               │           │           │           │            │◀─ack────
 │               │           │           │           │            │─verify
 │◀─────────────────────────────────────────────────────────done───│
```
