@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent

val pkg: String = providers.gradleProperty("amneziawgPackageName").get()
val cmakeAndroidPackageName: String = providers.environmentVariable("ANDROID_PACKAGE_NAME").getOrElse(pkg)

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "${pkg}.tunnel"
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
                    arguments("-DANDROID_PACKAGE_NAME=${cmakeAndroidPackageName}")
                }
            }
        }
        debug {
            externalNativeBuild {
                cmake {
                    arguments("-DANDROID_PACKAGE_NAME=${cmakeAndroidPackageName}.debug")
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
                val typeSuffix = if (buildType.name == "debug") "-debug" else ""
                f.outputFileName = "awg-tunnel" +
                        "-${file("tunnel-version").readText()}" +
                        typeSuffix +
                        ".aar"
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
            groupId = pkg
            artifactId = "awg-tunnel"
            version = file("tunnel-version").readText()

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
