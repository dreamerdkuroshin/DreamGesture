# GestureShare ProGuard Rules

# Keep domain models
-keep class com.gestureshare.core.domain.model.** { *; }
-keep class com.gestureshare.core.common.util.** { *; }

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep security classes
-keep class com.gestureshare.core.security.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Protobuf
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# Keep Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static void w(...);
    public static void e(...);
}

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.gestureshare.**$$serializer { *; }
-keepclassmembers class com.gestureshare.** {
    *** Companion;
}
-keepclasseswithmembers class com.gestureshare.** {
    kotlinx.serialization.KSerializer serializer(...);
}
