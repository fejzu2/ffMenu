import java.util.Properties
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

plugins {
    `java-library`
    `maven-publish`
}

val envFile = file("C:/Users/jakub/IdeaProjects/.env")
val envProps = Properties().apply {
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            if (line.contains("=") && !line.startsWith("#")) {
                val (key, value) = line.split("=", limit = 2)
                setProperty(key.trim(), value.trim())
            }
        }
    }
}

allprojects {
    group = "pl.fejzu"
    version = "1.8-BETA"

    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://repo.fejzu.pl/releases")
        maven("https://storehouse.okaeri.eu/repository/maven-public")
    }
}

subprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        val lombok = "1.18.36"
        compileOnly("org.projectlombok:lombok:$lombok")
        annotationProcessor("org.projectlombok:lombok:$lombok")
        testCompileOnly("org.projectlombok:lombok:$lombok")
        testAnnotationProcessor("org.projectlombok:lombok:$lombok")
    }

    if (project.name != "example") {
        apply(plugin = "maven-publish")

        publishing {
            repositories {
                maven {
                    if (version.toString().endsWith("-SNAPSHOT")) {
                        name = "reposilite-snapshots"
                        url = uri("https://repo.fejzu.pl/snapshots")
                    } else {
                        name = "reposilite-releases"
                        url = uri("https://repo.fejzu.pl/releases")
                    }
                    credentials {
                        username = System.getenv("MAVEN_USERNAME") ?: envProps.getProperty("MAVEN_USERNAME")
                        password = System.getenv("MAVEN_TOKEN") ?: envProps.getProperty("MAVEN_TOKEN")
                    }
                    isAllowInsecureProtocol = false
                }
            }

            publications {
                create<MavenPublication>("library") {
                    from(components["java"])
                    pom.withXml {
                        val repositories = asNode().appendNode("repositories")
                        project.repositories.findAll(closureOf<Any> {
                            if (this is MavenArtifactRepository && this.url.toString().startsWith("https")) {
                                val repository = repositories.appendNode("repository")
                                repository.appendNode(
                                    "id",
                                    this.url.toString().replace("https://", "").replace("/", "-").replace(".", "-").trim()
                                )
                                repository.appendNode("url", this.url.toString().trim())
                            }
                        })
                    }
                }
            }
        }

        tasks.register("deleteExistingVersion") {
            group = "publishing"
            description = "Delete existing version from repository before publishing"

            doLast {
                val repoUrl = if (version.toString().endsWith("-SNAPSHOT")) {
                    "https://repo.fejzu.pl/snapshots"
                } else {
                    "https://repo.fejzu.pl/releases"
                }

                val groupPath = project.group.toString().replace(".", "/")
                val artifactPath = "$groupPath/${project.name}/${project.version}"
                val deleteUrl = "$repoUrl/$artifactPath"

                val username = System.getenv("MAVEN_USERNAME") ?: envProps.getProperty("MAVEN_USERNAME")
                val password = System.getenv("MAVEN_TOKEN") ?: envProps.getProperty("MAVEN_TOKEN")

                try {
                    val connection = URL(deleteUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "DELETE"
                    connection.setRequestProperty("Authorization", "Basic " +
                            Base64.getEncoder().encodeToString("$username:$password".toByteArray()))

                    val responseCode = connection.responseCode
                    when (responseCode) {
                        200, 204 -> println("✓ Deleted existing version: $deleteUrl")
                        404 -> println("✓ Version does not exist yet: $deleteUrl")
                        else -> println("⚠ Failed to delete version. Response code: $responseCode")
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    println("⚠ Could not delete existing version: ${e.message}")
                }
            }
        }

        tasks.named("publish") {
            dependsOn("deleteExistingVersion")
        }

        tasks.named("publishToMavenLocal") {
            dependsOn("deleteExistingVersion")
        }
    }
}