package com.gestureshare.buildlogic.convention

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.base") {
                extensions.configure<BaseExtension> {
                    compileSdk = 34
                    defaultConfig {
                        minSdk = 31
                        targetSdk = 34
                        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        vectorDrawables { useSupportLibrary = true }
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_17
                        targetCompatibility = JavaVersion.VERSION_17
                    }
                    buildFeatures {
                        compose = true
                        buildConfig = true
                    }
                    composeOptions {
                        kotlinCompilerExtensionVersion = "1.5.8"
                    }
                    packaging {
                        resources {
                            excludes += "/META-INF/{AL2.0,LGPL2.1}"
                            excludes += "/META-INF/DEPENDENCIES"
                            excludes += "/META-INF/INDEX.LIST"
                        }
                    }
                }
            }
        }
    }
}
