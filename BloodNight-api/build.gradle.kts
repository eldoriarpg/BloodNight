plugins {
    id("de.eldoria.library-conventions")
}

description = "BloodNight-api"

java {
    withJavadocJar()
}

publishData {
    useEldoNexusRepos()
    publishComponent("java")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            publishData.configurePublication(this)
        }
    }

    repositories {
        maven {
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
