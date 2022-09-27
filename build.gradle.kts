import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.0.8"
}

group = "de.eldoria"
version = "0.12.0"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
