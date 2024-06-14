group = "me.moeszyslak"
version = "2.0.3"
description = "A multi-guild logging bot"

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.24.0")
    implementation("com.google.guava:guava:33.2.1-jre")
}

tasks {
    kotlin {
        jvmToolchain(17)
    }

    compileKotlin {
        doLast("writeProperties") {}
    }

    register<WriteProperties>("writeProperties") {
        property("name", project.name)
        property("description", project.description.toString())
        property("version", version.toString())
        property("url", "https://github.com/the-programmers-hangout/LogBot")
        setOutputFile("src/main/resources/bot.properties")
    }

    shadowJar {
        archiveFileName.set("LogBot.jar")
        manifest {
            attributes("Main-Class" to "me.moeszyslak.logbot.MainKt")
        }
    }
}