plugins {
    java
    id("de.eldoria.library-conventions")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

description = "BloodNight-api"
val log4jVersion = "2.14.0"

java {
    sourceCompatibility = JavaVersion.VERSION_15
}

dependencies {
    api(project(":BloodNight-mobs"))
    implementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    implementation("com.sparkjava", "spark-core", "2.9.3")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
}

tasks {
    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.eldoria.bloodnight.webservice.WebService"))
        }
    }
}
