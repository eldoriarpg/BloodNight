import de.chojo.PublishData

plugins {
    java
    id("de.chojo.publishdata") version "1.0.4"
}

group = "de.eldoria"
version = "0.10.9"

subprojects {
    apply {
        plugin<PublishData>()
    }
}
