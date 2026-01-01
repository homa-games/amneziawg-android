import java.nio.charset.StandardCharsets

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

tasks.register("syncReadme") {
    doLast {
        val readmeFile = file("README.md")
        val tunnelVersion = providers.gradleProperty("tunnelVersion").get()

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
