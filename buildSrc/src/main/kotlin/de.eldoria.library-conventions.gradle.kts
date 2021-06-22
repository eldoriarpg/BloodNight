plugins {
    `java-library`
    `maven-publish`
    id("de.eldoria.java-conventions")
}

publishing {
    publications {
        val publishData = PublishData(project)

        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group as String?
            artifactId = project.name.toLowerCase()
            version = publishData.getVersion()
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
            val publishData = PublishData(project)
            name = "EldoNexus"
            url = uri(publishData.getRepository())

            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}