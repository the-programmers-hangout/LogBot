package me.moeszyslak.logbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.moeszyslak.logbot.dataclasses.Configuration
import java.util.*

fun setupPrecondition(configuration: Configuration) = precondition {
    val command = command ?: return@precondition fail()
    val guild = guild ?: return@precondition fail()

    if (configuration.hasGuildConfig(guild.id)) return@precondition

    if (!command.names.any { it.lowercase() == "setup" })
        fail("You must first use the `Setup` command in this guild.")
}