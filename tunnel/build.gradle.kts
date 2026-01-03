@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.nio.charset.StandardCharsets

val packageName: String = providers.gradleProperty("amneziawgPackageName").get()
val tunnelVersion: String = if (project.hasProperty("TUNNEL_VERSION")) {
    project.property("TUNNEL_VERSION") as String
} else {
    providers.gradleProperty("tunnelVersion").get()
}

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    namespace = "${packageName}.tunnel"
    externalNativeBuild {
        cmake {
            path("tools/CMakeLists.txt")
        }
    }
    testOptions.unitTests.all {
        it.testLogging { events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED) }
    }
    buildTypes {
        all {
            externalNativeBuild {
                cmake {
                    targets("libwg-go.so", "libwg.so", "libwg-quick.so")
                    arguments("-DGRADLE_USER_HOME=${project.gradle.gradleUserHomeDir}")
                }
            }
        }
        release {
            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_PACKAGE_NAME=${packageName}")
                }
            }
        }
        debug {
            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_PACKAGE_NAME=${packageName}.debug")
                }
            }
        }
    }
    lint {
        disable += "LongLogTag"
        disable += "NewApi"
    }
    // https://arulmani33.medium.com/change-apk-name-in-android-gradle-8-1-kts-97c55fcaaa27
    libraryVariants.all {
        outputs.map { o -> o as com.android.build.gradle.internal.api.LibraryVariantOutputImpl }
            .forEach { f ->
                f.outputFileName = "awg-tunnel-${tunnelVersion}-${buildType.name}.aar"
            }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.collection)
    compileOnly(libs.jsr305)
    testImplementation(libs.junit)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = packageName
            artifactId = "awg-tunnel"
            version = tunnelVersion

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "demo"
            url = uri("../demorepo")
        }
    }
}

tasks.register("syncReadme") {
    val readmeFile = file("../README.md")
    val tunnelVersion = providers.gradleProperty("tunnelVersion").get()
    inputs.property("tunnelVersion", tunnelVersion)
    outputs.file(readmeFile)

    doLast {
        val updatedLines = readmeFile.readLines().map { line ->
            if (line.startsWith("implementation")) {
                "implementation(\"org.amnezia.awg:awg-tunnel:${tunnelVersion}\")"
            } else {
                line
            }
        }
        readmeFile.writeText(
            updatedLines.joinToString(separator = "\r\n") + "\r\n",
            StandardCharsets.UTF_8
        )
    }
}

tasks.named("publishReleasePublicationToDemoRepository") {
    dependsOn(tasks.named("syncReadme"))
}
