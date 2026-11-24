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
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

sourceSets {
    main {
        java.srcDirs("../shared/paper-core/java", "../shared/paper-standalone/java")
        resources.srcDir("../shared/paper-standalone/resources")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    archiveBaseName.set("OpenMarry-Paper-26")
}
