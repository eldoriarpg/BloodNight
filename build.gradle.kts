import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.1.0"
}

group = "de.eldoria"
version = "0.11.2"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
