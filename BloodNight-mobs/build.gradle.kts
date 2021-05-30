plugins {
    java
    id("de.eldoria.library-conventions")
}

description = "BloodNight-api"

dependencies{
    api(project(":BloodNight-api"))
    api("com.fasterxml.jackson.module", "jackson-modules-java8", "2.12.3")

}

java{
    withJavadocJar()
    withSourcesJar()
}