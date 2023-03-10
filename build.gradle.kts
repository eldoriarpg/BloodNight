import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.2.1"
}

group = "de.eldoria"
version = "0.11.2"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
