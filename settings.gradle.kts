rootProject.name = "BloodNight"
include(":BloodNight-api")
include(":BloodNight-core")

pluginManagement{
    repositories{
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}
