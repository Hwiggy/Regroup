plugins {
    kotlin("jvm") version "1.8.10" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    `maven-publish`
}

allprojects {
    apply(plugin = "maven-publish")

    group = "me.hwiggy.regroup"
    version = "1.2-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://nexus.mcdevs.us/repository/mcdevs")
    }

    publishing {
        repositories {
            mavenLocal()
            when (project.findProperty("deploy") ?: "local") {
                "local" -> return@repositories
                "remote" -> maven {
                    if (project.version.toString().endsWith("-SNAPSHOT")) {
                        setUrl("https://nexus.mcdevs.us/repository/mcdevs-snapshots/")
                        mavenContent { snapshotsOnly() }
                    } else {
                        setUrl("https://nexus.mcdevs.us/repository/mcdevs-releases/")
                        mavenContent { releasesOnly() }
                    }
                    credentials {
                        username = System.getenv("NEXUS_USERNAME")
                        password = System.getenv("NEXUS_PASSWORD")
                    }
                }
            }
        }
    }
}