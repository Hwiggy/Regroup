plugins {
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    compileOnly(kotlin("stdlib"))
    api(project(":Modules:API"))
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}