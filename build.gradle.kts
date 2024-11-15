import de.chojo.PublishData

plugins {
    java
    alias(libs.plugins.publishdata)
}

group = "de.eldoria"
version = "0.12.0"

subprojects {
    apply {
        plugin<PublishData>()
        plugin<MavenPublishPlugin>()
        plugin<JavaLibraryPlugin>()
    }

    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo1.maven.org/maven2/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:1.18.36")
        compileOnly("org.jetbrains:annotations:26.0.1")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
    }

    java {
        withSourcesJar()
        withJavadocJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}
