
plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://repo1.maven.org/maven2/")
    maven ( "https://hub.spigotmc.org/nexus/content/repositories/snapshots/" )
    maven ( "https://eldonexus.de/repository/maven-public/" )
    maven ( "https://eldonexus.de/repository/maven-proxies/" )
    maven ( "https://oss.sonatype.org/content/repositories/snapshots/" )
    maven ( "https://mvn.lumine.io/repository/maven-public/" )
    maven ( "https://repo.extendedclip.com/content/repositories/placeholderapi/" )
    maven ( "https://mvn.intellectualsites.com/content/repositories/snapshots" )
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("org.jetbrains:annotations:16.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

group = "de.eldoria"
version = "0.10.4"

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks{
    compileJava{
        options.encoding = "UTF-8"
    }
}
