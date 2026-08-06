package com.gestureshare.buildlogic.convention

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.getByType<BaseExtension>()
            extension.buildFeatures.compose = true
            extension.composeOptions.kotlinCompilerExtensionVersion = "1.5.8"
        }
    }
}
