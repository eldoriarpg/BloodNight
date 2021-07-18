plugins {
    id("de.eldoria.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":BloodNight-api"))
    implementation("de.eldoria", "eldo-util", "1.9.2-SNAPSHOT")
    implementation("net.kyori", "adventure-api", "4.8.1")
    implementation("net.kyori", "adventure-platform-bukkit", "4.0.0-SNAPSHOT")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testImplementation("junit", "junit", "4.13.1")
    testImplementation("org.mockito", "mockito-core", "3.5.13")
    compileOnly("io.lumine.xikage", "MythicMobs", "4.12.0")
    compileOnly("me.clip", "placeholderapi", "2.10.9")
    compileOnly("com.onarandombox.multiversecore", "Multiverse-Core", "4.2.1")
    compileOnly("se.hyperver.hyperverse", "Core", "0.9.0-SNAPSHOT")
}

description = "BloodNight-core"
val shadebase = project.group as String + ".bloodnight."

java {
    withJavadocJar()
}

tasks{
    shadowJar {
        relocate("de.eldoria.eldoutilities", shadebase + "eldoutilities")
        relocate("net.kyori", shadebase + "kyori")
        mergeServiceFiles()
        archiveBaseName.set(project.parent?.name)
    }
}
