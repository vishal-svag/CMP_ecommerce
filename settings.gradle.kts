rootProject.name = "KMP_ready"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Modules must not declare their own repositories — keeps supply chain auditable in one place.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// App shells
include(":androidApp")
include(":shared")

// Core (horizontal, reusable across features)
include(":core:common")
include(":core:domain")
include(":core:security")
include(":core:network")
include(":core:data")
include(":core:designsystem")
include(":core:ui")

// Features (vertical slices — never depend on each other)
include(":feature:auth")
include(":feature:home")
