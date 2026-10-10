@file:Suppress("UnstableApiUsage")

import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.bundling.AbstractArchiveTask
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
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    ndkVersion = libs.versions.ndk.get()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
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
                    arguments("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

androidComponents {
    onVariants { variant ->
        val variantName = variant.name
        val taskSuffix = variantName.replaceFirstChar { it.uppercaseChar() }
        val aarFile = variant.artifacts.get(SingleArtifact.AAR)
        val aarFileName = "awg-tunnel-${tunnelVersion}-${variantName}.aar"
        val exportAar = tasks.register<Sync>("export${taskSuffix}Aar") {
            group = "build"
            description = "Exports the ${variantName} AAR with its version in the filename."
            inputs.property("aarFileName", aarFileName)
            from(aarFile)
            into(layout.buildDirectory.dir("outputs/aar/${variantName}"))
            rename { aarFileName }
        }
        tasks.named { it == "assemble${taskSuffix}" }.configureEach {
            dependsOn(exportAar)
        }
    }
}

dependencies {
    compileOnly(libs.androidx.annotation)
    compileOnly(libs.jsr305)
    testImplementation(libs.junit)
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
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
