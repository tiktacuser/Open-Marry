plugins {
    java
}

group = "ru.openmarry"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

sourceSets {
    main {
        java.srcDirs("../shared/paper-core/java", "../shared/paper-proxy/java")
        resources.srcDir("../shared/paper-proxy/resources")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    archiveBaseName.set("OpenMarry-Paper-Proxy-1.21.1")
}
