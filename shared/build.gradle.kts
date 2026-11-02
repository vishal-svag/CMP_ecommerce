plugins {
    alias(libs.plugins.kmpready.kmp.compose)
    alias(libs.plugins.kotlinSerialization)
}

/**
 * Composition root: wires every module into one Koin graph, owns root navigation,
 * and is the only module exported to iOS as a framework.
 */
kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "ai.reslt.kmpready.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common")) // AppConfig is part of initKoin's public signature
            implementation(project(":core:domain"))
            implementation(project(":core:security"))
            implementation(project(":core:network"))
            implementation(project(":core:data"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":feature:auth"))
            implementation(project(":feature:home"))

            api(libs.koin.core) // KoinAppDeclaration is part of initKoin's public signature
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
