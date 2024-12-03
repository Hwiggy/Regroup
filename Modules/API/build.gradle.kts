plugins {
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    `maven-publish`
}


dependencies {
    compileOnly(kotlin("stdlib"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}