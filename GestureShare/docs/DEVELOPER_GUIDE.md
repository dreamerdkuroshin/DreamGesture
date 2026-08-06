# Developer Guide

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Git

### Setup

```bash
git clone https://github.com/gestureshare/android.git
cd android
./gradlew sync
```

### Project Structure

The project uses a multi-module Gradle setup:

- **core/**: Shared modules used across features
- **feature/**: Self-contained feature modules
- **app/**: Application module that wires everything together

## Adding a New Feature

1. Create module directory: `feature/my-feature/`
2. Create `build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.gestureshare.feature.myfeature"
    compileSdk = 34
    defaultConfig { minSdk = 31 }
}

dependencies {
    implementation(project(":core:domain"))
    // Add feature-specific dependencies
}
```

3. Register in `settings.gradle.kts`:
```kotlin
include(":feature:my-feature")
```

4. Add to app dependencies:
```kotlin
implementation(project(":feature:my-feature"))
```

## Adding a New Gesture

1. Add enum value to `GestureType` in `core/domain/`
2. Add classification rule in `GestureRecognizer.classifyGesture()`
3. Add handler in `GestureEngine.processGesture()`
4. Add UI animation in `GestureAnimationController`

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Coverage
```bash
./gradlew jacocoTestReport
```

### Writing Tests

```kotlin
@Test
fun `new gesture detection works`() = runBlocking {
    val gesture = createGesture(GestureType.NEW, confidence = 0.97f)
    val result = engine.processGesture(gesture)
    assertThat(result).isTrue()
}
```

## Code Style

- Kotlin official style guide
- ktlint for formatting
- Detekt for static analysis
- Max line length: 120
- No wildcard imports

## Commit Convention

```
feat: Add swipe gesture recognition
fix: Resolve memory leak in camera manager
perf: Reduce inference time by 15%
test: Add coverage for gesture engine
docs: Update architecture diagram
```

## Branch Strategy

- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `release/*`: Release preparation

## Pull Request Template

```markdown
## What changed?
- Description of changes

## Why?
- Reason for changes

## How to test?
- Steps to verify

## Checklist
- [ ] Tests pass
- [ ] Lint clean
- [ ] Documentation updated
- [ ] No breaking changes
```

## Debugging

### Enable Verbose Logging
```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### MediaPipe GPU Fallback
If MediaPipe fails to initialize on GPU:
1. Check device compatibility
2. The code automatically falls back to CPU
3. Check logcat for `EGL` errors

### TFLite Model Loading
Ensure model files are in `app/src/main/assets/models/`:
- `hand_landmarker.task` (MediaPipe Hands)
- `gesture_classifier.tflite` (Custom gesture classifier)

## Build Variants

| Variant | Debug | Signing | Minification |
|---------|-------|---------|-------------|
| debug | Yes | Debug keystore | No |
| release | No | Release keystore | Yes |

## Common Issues

### Camera permission denied
- Check `POST_NOTIFICATIONS` permission on Android 13+
- Check `FOREGROUND_SERVICE_CAMERA` on Android 14+

### MediaPipe initialization fails
- Verify model file exists in assets
- Check disk space for model extraction
- Ensure GPU drivers are up to date

### Transfer fails
- Check WiFi Direct is enabled
- Verify location permission (required for WiFi scanning)
- Check if devices support WiFi Direct
