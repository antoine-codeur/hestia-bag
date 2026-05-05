/*
 * settings.gradle.kts — Project-level settings.
 * Only contains the project name and plugin resolution repositories.
 */
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "HestiaBag"
