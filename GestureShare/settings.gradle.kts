pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://storage.googleapis.com/maven") }
    }
}

rootProject.name = "GestureShare"
include(":app")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:di")
include(":core:ui")
include(":core:security")
include(":core:analytics")
include(":feature:screenshot-listener")
include(":feature:camera")
include(":feature:gesture-vision")
include(":feature:gesture-engine")
include(":feature:nearby-discovery")
include(":feature:transfer-engine")
include(":feature:ui-animation")
include(":feature:main-screen")
