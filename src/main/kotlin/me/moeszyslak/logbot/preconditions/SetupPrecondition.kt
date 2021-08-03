package me.moeszyslak.logbot.preconditions

import me.jakejmattson.discordkt.api.dsl.precondition
import me.moeszyslak.logbot.dataclasses.Configuration

fun setupPrecondition(configuration: Configuration) = precondition {
    val command = command ?: return@precondition fail()
    val guild = guild ?: return@precondition fail()

    if (configuration.hasGuildConfig(guild.id.value)) return@precondition

    if (!command.names.any { it.toLowerCase() == "setup" })
        fail("You must first use the `Setup` command in this guild.")
}