plugins {
    id("net.fabricmc.fabric-loom-remap")
}

group = "ru.openmarry"
version = "1.0.0"

base {
    archivesName.set("OpenMarry-Fabric-1.21.1")
}

repositories {
    mavenCentral()
    maven("https://maven.nucleoid.xyz/") {
        name = "Nucleoid"
    }
}

val fabricLoaderVersion: String by project
val fabricApiVersion121: String by project
val placeholderApiVersion121: String by project

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion121")
    modImplementation("eu.pb4:placeholder-api:$placeholderApiVersion121")
}

sourceSets {
    main {
        java.srcDir("../shared/fabric-core/java")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
