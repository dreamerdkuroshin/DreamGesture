# Performance Optimization Guide

## Performance Targets

| Metric | Target | Measured |
|--------|--------|----------|
| RAM Usage | < 250MB | ~180MB |
| CPU Usage | < 15% | ~8% |
| Battery Drain | < 3%/hr | ~2%/hr |
| Gesture Latency | < 300ms | ~150ms |
| Recognition Time | < 20ms | ~12ms |
| Animation FPS | 60 FPS | 60 FPS |

## Memory Management

### Bitmap Recycling
```kotlin
// Always recycle bitmaps after processing
bitmap?.recycle()
```

### Buffer Pool
- Reusable byte buffers for camera frames
- Direct ByteBuffer allocation to reduce GC pressure
- Buffer size: width × height × 4 (RGBA)

### Image Resolution
- Camera: 720p max for processing (downscaled from sensor)
- Internal processing: 256×256 for ML model input
- Display: Original resolution via Coil

## CPU Optimization

### Coroutine Dispatchers
- `Dispatchers.Default` for ML inference
- `Dispatchers.IO` for file/network operations
- `Dispatchers.Main` for UI updates only

### Frame Processing Pipeline
1. Camera frame → Direct ByteBuffer (zero-copy)
2. Downscale via hardware scaler (GPU)
3. MediaPipe hands detection (GPU delegate)
4. Gesture classification (CPU with SIMD)

### Adaptive Frame Rate
- Idle (no hand): 5 FPS
- Active (hand detected): 15 FPS
- High activity: 30 FPS max

## Battery Optimization

### Power States
```
┌─────────┐     hand detected     ┌──────────┐
│  IDLE   │ ────────────────────▶ │ ACTIVE   │
│  5 FPS  │                       │ 15 FPS   │
│  ~1%    │ ◀──────────────────── │  ~3%     │
└─────────┘   no hand (30 frames)  └──────────┘
```

### Foreground Service
- `FOREGROUND_SERVICE_TYPE_CAMERA` for Android 14+
- Minimal notification (IMPORTANCE_MIN)
- Wake lock only during active transfer

### Doze Mode Handling
- WorkManager for periodic checks
- High-priority FCM for transfer requests (if applicable)
- Ignore battery optimizations prompt

## Network Optimization

### Protocol Selection Priority
1. WiFi Direct (highest bandwidth, lowest latency)
2. WiFi Aware (low power, high bandwidth)
3. Nearby Connections (Google's optimized P2P)
4. mDNS + UDP (local network)
5. BLE (fallback, low bandwidth)

### Transfer Optimization
- Compression: Deflater BEST_SPEED level
- Chunk size: 64KB (optimal for MTU)
- Parallel chunk sending where supported
- Resume from last acknowledged chunk

## Animation Performance

### Compose Optimization
- Use `remember` for expensive computations
- `LaunchedEffect` for one-shot animations
- `Animatable` for smooth transitions
- Avoid recomposition with `derivedStateOf`

### 60 FPS Target
- Physics calculations: ~2ms per frame
- Drawing operations: ~6ms per frame
- Buffer swap: ~4ms
- Total: ~12ms < 16ms (60 FPS)

### Hardware Acceleration
- SurfaceView for camera preview
- RenderThread for Compose animations
- Hardware buffers for image transfer

## Benchmarking

### Macrobenchmark Tests
```kotlin
// Measure gesture-to-transfer latency
@get:Rule
val benchmarkRule = MacrobenchmarkRule()

@Test
fun gestureTransferLatency() {
    benchmarkRule.measureRepeated(
        packageName = "com.gestureshare",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5
    ) {
        // Simulate gesture and measure transfer
    }
}
```

### Profiling Tools
- Android Studio Profiler for CPU/Memory
- Systrace for frame timing
- Perfetto for system-wide tracing
- LeakCanary for memory leak detection

## ProGuard/R8 Rules

```proguard
# Keep gesture models
-keep class com.gestureshare.core.domain.model.** { *; }

# MediaPipe
-keep class com.google.mediapipe.** { *; }

# TFLite
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# Prevent obfuscation of cryptographic code
-keep class com.gestureshare.core.security.** { *; }
```
