plugins {
    id("de.eldoria.library-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":BloodNight-api"))
    implementation("de.eldoria", "eldo-util", "1.10.2")
    implementation("net.kyori", "adventure-platform-bukkit", "4.1.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.9.1")
    testImplementation("junit", "junit", "4.13.2")
    testImplementation("org.mockito", "mockito-core", "4.8.0")
    compileOnly("io.lumine", "Mythic-Dist", "5.1.4")
    compileOnly("me.clip", "placeholderapi", "2.11.2")
    compileOnly("com.onarandombox.multiversecore", "Multiverse-Core", "4.3.1")
    compileOnly("se.hyperver.hyperverse", "Core", "0.10.0")
}

configurations {
    all {
        resolutionStrategy{
            cacheChangingModulesFor(0, "SECONDS")
        }
    }
}

description = "BloodNight-core"
val shadebase = project.group as String + ".bloodnight."

java {
    withJavadocJar()
}

publishData {
    useEldoNexusRepos()
    publishComponent("java")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            publishData.configurePublication(this)
        }
    }

    repositories {
        maven {
            name = "EldoNexus"
            url = uri(publishData.getRepository())

            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}

tasks {
    shadowJar {
        relocate("de.eldoria.eldoutilities", shadebase + "eldoutilities")
        relocate("net.kyori", shadebase + "kyori")
        mergeServiceFiles()
        archiveBaseName.set(project.parent?.name)
    }

    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("plugin.yml") {
                expand(
                    "version" to publishData.getVersion(true)
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    build{
        dependsOn(shadowJar)
    }
}
