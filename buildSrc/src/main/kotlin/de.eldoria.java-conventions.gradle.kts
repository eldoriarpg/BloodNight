plugins {
    `java-library`
    `maven-publish`
}

group = "de.eldoria"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8
val lombokVersion = "1.18.20"

repositories {
    maven { url = uri("https://eldonexus.de/repository/maven-releases/") }
    maven { url = uri("https://eldonexus.de/repository/maven-snapshots/") }
    maven { url = uri("https://eldonexus.de/repository/spigot/") }
    maven { url = uri("https://eldonexus.de/repository/OnARandomBox/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://mvn.lumine.io/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://mvn.intellectualsites.com/content/repositories/snapshots") }
}

dependencies {
    implementation("de.eldoria:eldo-util:1.8.4")
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok", "lombok", lombokVersion)
    annotationProcessor("org.projectlombok", "lombok", lombokVersion)
    compileOnly("org.jetbrains", "annotations", "16.0.2")
}

allprojects {
    java {
        withSourcesJar()
        withJavadocJar()
    }
}

tasks {
    publish {
        dependsOn(build)
    }

    compileJava {
        options.encoding = "UTF-8"
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}