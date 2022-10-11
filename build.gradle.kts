import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.0.8"
}

group = "de.eldoria"
version = "0.11.1"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
