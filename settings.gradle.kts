rootProject.name = "ffMenu"

// -- core --
include(":core")

// -- bukkit --
include(":bukkit")

// -- config --
include(":config")

// -- bukkit example --
include(":example")

// -- plugin management --
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

