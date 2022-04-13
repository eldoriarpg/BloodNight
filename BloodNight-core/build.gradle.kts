plugins {
    id("de.eldoria.library-conventions")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":BloodNight-api"))
    implementation("de.eldoria", "eldo-util", "1.9.6-DEV")
    implementation("net.kyori", "adventure-api", "4.10.1")
    implementation("net.kyori", "adventure-platform-bukkit", "4.1.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
    testImplementation("junit", "junit", "4.13.2")
    testImplementation("org.mockito", "mockito-core", "4.4.0")
    compileOnly("io.lumine.xikage", "MythicMobs", "4.12.0")
    compileOnly("me.clip", "placeholderapi", "2.10.10")
    compileOnly("com.onarandombox.multiversecore", "Multiverse-Core", "4.2.1")
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
                    "version" to PublishData(project).getVersion(true)
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
}
