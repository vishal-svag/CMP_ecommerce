plugins {
    alias(libs.plugins.kmpready.kmp.compose)
}

kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:domain"))
        }
    }
}

compose.resources {
    packageOfResClass = "ai.reslt.kmpready.core.ui.resources"
    publicResClass = false
}
