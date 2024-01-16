import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.3.0"
}

group = "de.eldoria"
version = "0.11.4"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
