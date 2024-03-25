import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.4.0"
}

group = "de.eldoria"
version = "0.12.0"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
