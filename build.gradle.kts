group = "me.moeszyslak"
version = Versions.BOT
description = "A multi-guild discord bot to log everything and everything you could ever want"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:${Versions.DISCORDKT}")
    implementation("com.google.guava:guava:30.0-jre")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        archiveFileName.set("LogBot.jar")
        manifest {
            attributes(
                "Main-Class" to "me.moeszyslak.logbot.MainKt"
            )
        }
    }
}

object Versions {
    const val BOT = "1.0.0"
    const val DISCORDKT = "0.23.2"
}