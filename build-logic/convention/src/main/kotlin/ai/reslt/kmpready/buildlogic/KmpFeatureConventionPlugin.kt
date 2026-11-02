package ai.reslt.kmpready.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A vertical feature slice (presentation only). Features depend on core modules,
 * never on each other and never on :core:data / :core:network directly — they talk
 * to the domain layer through use cases.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("kmpready.kmp.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            commonMainDependencies {
                implementation(project(":core:common"))
                implementation(project(":core:domain"))
                implementation(project(":core:ui"))
                implementation(project(":core:designsystem"))

                implementation(libs.lib("koin-core"))
                implementation(libs.lib("koin-compose"))
                implementation(libs.lib("koin-compose-viewmodel"))
                implementation(libs.lib("navigation-compose"))
                implementation(libs.lib("kotlinx-serialization-json"))
            }
        }
    }
}
