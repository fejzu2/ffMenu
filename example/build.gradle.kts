plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "litecommands"
        url = uri("https://repo.panda-lang.org/releases/")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    implementation("pl.ffcode:platform-core:2.1.8")
    implementation("pl.ffcode:platform-bukkit:2.1.8")
    implementation("pl.ffcode:platform-bukkit-config:2.1.8")


    implementation(project(":core"))
    implementation(project(":bukkit"))
    implementation(project(":config"))

    implementation("dev.rollczi:litecommands-bukkit:3.10.6")

    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:6.0.0-beta.3")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:6.0.0-beta.3")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:6.0.0-beta.3")
    implementation("eu.okaeri:okaeri-injector:2.1.0")
}

tasks.shadowJar {
    archiveFileName.set("ffMenu-test-${project.version}.jar")
    mergeServiceFiles()

    relocate("eu.okaeri", "pl.fejzu.test.libs.eu.okaeri")
    relocate("dev.rollczi", "pl.fejzu.test.libs.dev.rollczi")
    relocate("pl.fejzu.menu", "pl.fejzu.test.libs.pl.fejzu.menu")
    relocate("pl.ffcode.platform", "pl.fejzu.test.libs.pl.ffcode.platform")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}