plugins {
    kotlin("jvm") version "1.9.23"
}

group = "blue.melon"
version = "1.0"

repositories {
    mavenLocal()
//    maven("https://maven.playpro.com")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(
        group = "io.papermc.paper",
        name = "paper-api",
        version = "1.20.4-R0.1-SNAPSHOT"
    )
    compileOnly(
        group = "com.google.code.gson",
        name = "gson",
        version = "2.10.1"
    )
    compileOnly(
        group = "net.coreprotect",
        name = "coreprotect",
        version = "22.2"
    )
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}