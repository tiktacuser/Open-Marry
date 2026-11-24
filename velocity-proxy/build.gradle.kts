plugins {
    java
}

group = "ru.openmarry"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.3.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.3.2-SNAPSHOT")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    archiveBaseName.set("OpenMarry-Velocity")
}
