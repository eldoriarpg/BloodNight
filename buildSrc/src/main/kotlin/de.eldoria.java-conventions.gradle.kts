
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
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven ( "https://mvn.lumine.io/repository/maven-public/" )
    maven ( "https://repo.extendedclip.com/content/repositories/placeholderapi/" )
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.jetbrains:annotations:23.0.0")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

version = rootProject.version
group = "de.eldoria"

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks{
    compileJava{
        options.encoding = "UTF-8"
    }
}
