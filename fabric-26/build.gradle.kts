plugins {
    id("net.fabricmc.fabric-loom")
}

group = "ru.openmarry"
version = "1.0.0"

base {
    archivesName.set("OpenMarry-Fabric-26")
}

repositories {
    mavenCentral()
    maven("https://maven.nucleoid.xyz/") {
        name = "Nucleoid"
    }
}

val minecraftVersion26: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion26: String by project
val placeholderApiVersion26: String by project

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion26")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion26")
    implementation("eu.pb4:placeholder-api:$placeholderApiVersion26")
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
    options.release.set(25)
}
