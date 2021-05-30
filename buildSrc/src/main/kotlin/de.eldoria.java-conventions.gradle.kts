plugins {
    `java-library`
    `maven-publish`
}

group = "de.eldoria"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8
val lombokVersion = "1.18.20"

repositories {
    maven { url = uri("https://eldonexus.de/repository/maven-public/") }
    maven { url = uri("https://eldonexus.de/repository/maven-proxies/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://mvn.lumine.io/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://mvn.intellectualsites.com/content/repositories/snapshots") }
}

dependencies {
    implementation("de.eldoria:eldo-util:1.8.8-DEV")
    compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok", "lombok", lombokVersion)
    annotationProcessor("org.projectlombok", "lombok", lombokVersion)
    compileOnly("org.jetbrains", "annotations", "16.0.2")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
    testImplementation("com.github.seeseemelk", "MockBukkit-v1.16", "1.0.0")
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