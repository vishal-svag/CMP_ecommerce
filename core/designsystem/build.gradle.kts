plugins {
    alias(libs.plugins.kmpready.kmp.compose)
}

// Domain-agnostic UI kit: tokens + stateless components. No ViewModels, no domain models.
kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.designsystem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
    }
}

compose.resources {
    packageOfResClass = "ai.reslt.kmpready.core.designsystem.resources"
    publicResClass = false // exposed through AppIcons / AppTheme, never directly
}
