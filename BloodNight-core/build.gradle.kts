plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("de.eldoria.library-conventions")
}

dependencies {
    api(project(":BloodNight-api"))
    api(project(":BloodNight-mobs"))
    implementation("net.kyori:adventure-api:4.7.0")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.mockito:mockito-core:3.5.13")
    compileOnly("io.lumine.xikage:MythicMobs:4.9.1")
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.2.1")
    compileOnly("se.hyperver.hyperverse:Core:0.9.0-SNAPSHOT") { isTransitive = false }
}

var mainPackage = "bloodnight"
val shadebade = project.group as String + "." + mainPackage + "."
val descr = "Nights are not hard enough? Make them harder!"

java{
    withSourcesJar()
    withJavadocJar()
}

tasks {
    processResources {
        val publishData = PublishData(project)
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("plugin.yml") {
                expand(
                    "name" to project.parent?.name,
                    "version" to publishData.getVersion(true),
                    "description" to descr,
                    "url" to "https://www.spigotmc.org/resources/85095/"
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    shadowJar {
        relocate("net.kyori", shadebade + "kyori")
        relocate("de.eldoria.eldoutilities", shadebade + "eldoutilities")
        mergeServiceFiles()
        archiveBaseName.set(project.parent?.name)
    }

    test {
        useJUnit()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}