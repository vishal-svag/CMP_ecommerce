package ai.reslt.kmpready.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** A shared module that renders Compose Multiplatform UI. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("kmpready.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            commonMainDependencies {
                implementation(libs.lib("compose-runtime"))
                implementation(libs.lib("compose-foundation"))
                implementation(libs.lib("compose-material3"))
                implementation(libs.lib("compose-ui"))
                implementation(libs.lib("compose-components-resources"))
                implementation(libs.lib("compose-uiToolingPreview"))
                implementation(libs.lib("androidx-lifecycle-viewmodelCompose"))
                implementation(libs.lib("androidx-lifecycle-runtimeCompose"))
            }
        }
    }
}
