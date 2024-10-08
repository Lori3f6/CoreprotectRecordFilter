plugins {
    kotlin("jvm") version "2.0.20"
}

group = "blue.melon"
version = "1.0"

repositories {
    mavenLocal()
    //maven("https://maven.playpro.com")
    maven("https://ci.nyaacat.com/maven/")
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
        name = "CoreProtect",
        version = "22.4"
    )
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
