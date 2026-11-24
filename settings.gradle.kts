pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        gradlePluginPortal()
        mavenCentral()
    }

    val loomVersion: String by settings

    plugins {
        id("net.fabricmc.fabric-loom") version loomVersion
        id("net.fabricmc.fabric-loom-remap") version loomVersion
    }
}

rootProject.name = "OpenMarry"

include(
    "paper-standalone-1.21.1",
    "paper-standalone-26",
    "paper-proxy-1.21.1",
    "paper-proxy-26",
    "velocity-proxy",
    "fabric-1.21.1",
    "fabric-26"
)
