plugins {
    kotlin("jvm") version "1.8.10" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    `maven-publish`
}

allprojects {
    apply(plugin = "maven-publish")

    group = "me.hwiggy.regroup"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://nexus.mcdevs.us/repository/mcdevs")
    }
}