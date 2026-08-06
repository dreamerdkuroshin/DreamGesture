# DreamGesture Android APK

## This is a placeholder for the DreamGesture Android APK.

## To build the real APK:

### Prerequisites:
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- NDK 25.x

### Build Steps:

1. Open terminal in the GestureShare folder:
   cd GestureShare

2. Build the release APK:
   ./gradlew assembleRelease

3. Find the APK at:
   app/build/outputs/apk/release/app-release.apk

### To install the APK:

1. Transfer the APK to your Android device
2. Open file manager and tap the APK
3. Enable "Unknown sources" if prompted
4. Tap "Install"
5. Grant Camera, Storage, and Location permissions

### System Requirements:
- Android 12+ (API 31+)
- Front camera
- WiFi connection

For more help, visit: https://github.com/dreamerdkuroshin/DreamGesture
