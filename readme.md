![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/BloodNight/Verify%20state?style=for-the-badge&label=Building)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/eldoriarpg/BloodNight/Publish%20to%20Nexus?style=for-the-badge&label=Publishing)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/maven-releases/de.eldoria/bloodnight-core?label=Release&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Development)](https://img.shields.io/nexus/maven-dev/de.eldoria/bloodnight-core?label=DEV&logo=Release&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/de.eldoria/bloodnight-core?color=orange&label=Snapshot&server=https%3A%2F%2Feldonexus.de&style=for-the-badge)

# Blood Night

A Minecraft plugin which makes some nights an absolute nightmare. Let's make nights a challenge again.

- 22 Custom Mobs
- Full configurable
- Different night selection based on random, interval or moon phase

## Blood Night as Dependency

**Latest Version:**\
If you want to use blood night as a dependency you can use these.\
Make sure to replace the version with the release version from above.
### Gradle
``` kotlin
repositories {
    maven { url = uri("https://eldonexus.de/repository/maven-public") }
}

dependencies {
    implementation("de.eldoria", "bloodnight-core", "{version}")
}
```

### Maven
``` xml
<repository>
    <id>EldoNexus</id>
    <url>https://eldonexus.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>bloodnight-core</artifactId>
    <version>{version}</version>
</dependency>
```