plugins {
    `java-library`
    `maven-publish`
    id("de.eldoria.java-conventions")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            /*artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])*/
            from(components["java"])
            groupId = project.group as String?
            artifactId = project.name.toLowerCase()
            version = project.version as String?
            pom {
                url.set("https://github.com/eldoriarpg/BloodNight")
                developers {
                    developer {
                        name.set("Florian Fülling")
                        organization.set("EldoriaRPG")
                        organizationUrl.set("https://github.com/eldoriarpg")
                    }
                }
                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0")
                        url.set("https://github.com/eldoriarpg/BloodNight/blob/master/LICENSE.md")
                    }
                }
            }
            println(configurations.getByName("api").allDependencies)
        }

    }

    repositories {
        maven {
            val isSnapshot = version.toString().endsWith("SNAPSHOT");
            val release = "https://eldonexus.de/repository/maven-releases/";
            val snapshot = "https://eldonexus.de/repository/maven-snapshots/";
            name = "EldoNexus"
            url = uri(if (isSnapshot) snapshot else release)

            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}