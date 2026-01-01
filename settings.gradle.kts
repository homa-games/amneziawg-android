import com.android.build.api.dsl.SettingsExtension

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.settings") version "8.13.2"
}

rootProject.name = "amneziawg-android"

include(":tunnel")
include(":ui")

configure<SettingsExtension> {
    buildToolsVersion = "36.1.0"
    compileSdk = 36
    minSdk = 24
    ndkVersion = "29.0.14206865"
}
