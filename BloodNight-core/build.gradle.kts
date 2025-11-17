plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.runpaper)
}

dependencies {
    implementation(project(":BloodNight-api"))
    implementation("org.bstats", "bstats-bukkit", "3.1.0")
    bukkitLibrary(libs.bundles.eldoutil)
    bukkitLibrary("net.kyori", "adventure-platform-bukkit", "4.4.1")
    testImplementation("org.junit.jupiter", "junit-jupiter", "6.0.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "6.0.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "6.0.1")
    testImplementation("org.junit.platform", "junit-platform-launcher", "6.0.1")
    testImplementation("org.mockito", "mockito-core", "5.20.0")
    compileOnly("io.lumine", "Mythic-Dist", "5.10.1")
    compileOnly("me.clip", "placeholderapi", "2.11.7")
    compileOnly("org.mvplugins.multiverse.core", "multiverse-core", "5.3.4")
    compileOnly("se.hyperver.hyperverse", "Core", "0.10.0")
}

description = "BloodNight-core"
val shadebase = project.group as String + ".bloodnight."

publishData {
    addBuildData()
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
        relocate("org.bstats", "de.eldoria.bloodnight.libs.bstats")
        mergeServiceFiles()
        archiveBaseName.set(project.parent?.name)
    }

    build {
        dependsOn(shadowJar)
    }

    register<Copy>("copyToServer") {
        val path = project.property("targetDir") ?: "";
        if (path.toString().isEmpty()) {
            println("targetDir is not set in gradle properties")
            return@register
        }
        from(shadowJar)
        destinationDir = File(path.toString())
    }

    runServer {
        minecraftVersion("1.21.4")
    }
    test {
        useJUnitPlatform()
    }
}

bukkit {
    name = "BloodNight"
    description = "Make your nights a nightmare again"
    authors = listOf("RainbowDashLabs")
    version = publishData.getVersion(true)
    main = "de.eldoria.bloodnight.core.BloodNight"
    website = "https://www.spigotmc.org/resources/85095"
    apiVersion = "1.21"
    softDepend = listOf("Multiverse-Core", "Hyperverse", "MythicMobs", "PlaceholderAPI")

    commands {
        register("bloodnight") {
            description = "Main Blood Night command"
            usage = "/bloodnight help"
            aliases = listOf("bn", "bnight")
        }
    }
}
