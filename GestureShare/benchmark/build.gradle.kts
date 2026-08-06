plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gestureshare.benchmark"
    compileSdk = 34
    defaultConfig {
        minSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions {
        managedDevices {
            localDevices {
                create("pixel6") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "google"
                }
            }
        }
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.benchmark.junit)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.rules)
    implementation(libs.uiautomator)
}
