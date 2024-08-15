rootProject.name = "BloodNight"
include(":BloodNight-api")
include(":BloodNight-core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("eldoutil", "2.1.3")
            library("eldoutil.core", "de.eldoria.util", "core").versionRef("eldoutil")
            library("eldoutil.updater", "de.eldoria.util", "updater").versionRef("eldoutil")
            library("eldoutil.messaging", "de.eldoria.util", "messaging").versionRef("eldoutil")
            library("eldoutil.commands", "de.eldoria.util", "commands").versionRef("eldoutil")
            library("eldoutil.plugin", "de.eldoria.util", "plugin").versionRef("eldoutil")
            library("eldoutil.metrics", "de.eldoria.util", "metrics").versionRef("eldoutil")
            library("eldoutil.inventory", "de.eldoria.util", "inventory").versionRef("eldoutil")
            library("eldoutil.conversation", "de.eldoria.util", "conversation").versionRef("eldoutil")
            library("eldoutil.threading", "de.eldoria.util", "threading").versionRef("eldoutil")
            library("eldoutil.crossversion", "de.eldoria.util", "crossversion").versionRef("eldoutil")
            library("eldoutil.entities", "de.eldoria.util", "entities").versionRef("eldoutil")
            library("eldoutil.legacyserialization", "de.eldoria.util", "legacy-serialization").versionRef("eldoutil")
            bundle(
                "eldoutil", listOf(
                    "eldoutil.core", "eldoutil.updater", "eldoutil.messaging", "eldoutil.commands",
                    "eldoutil.plugin", "eldoutil.metrics", "eldoutil.inventory", "eldoutil.conversation",
                    "eldoutil.threading", "eldoutil.crossversion", "eldoutil.entities", "eldoutil.legacyserialization"
                )
            )

            plugin("publishdata", "de.chojo.publishdata").version("1.2.5")
            plugin("spotless", "com.diffplug.spotless").version("6.25.0")
            plugin("shadow", "io.github.goooler.shadow").version("8.1.8")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
        }
    }
}
