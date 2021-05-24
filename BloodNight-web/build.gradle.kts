plugins {
    java
    id("de.eldoria.library-conventions")
}

description = "BloodNight-api"
val log4jVersion = "2.14.0"

java {
    sourceCompatibility = JavaVersion.VERSION_15
}

dependencies {
    api(project(":BloodNight-mobs"))
    implementation("com.sparkjava", "spark-core", "2.9.3")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)

}