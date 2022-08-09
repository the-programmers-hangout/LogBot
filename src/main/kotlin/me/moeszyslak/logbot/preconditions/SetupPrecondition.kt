package me.moeszyslak.logbot.preconditions

import me.jakejmattson.discordkt.dsl.precondition
import me.moeszyslak.logbot.dataclasses.Configuration

fun setupPrecondition(configuration: Configuration) = precondition {
    val guild = guild ?: return@precondition fail()

    if (configuration.hasGuildConfig(guild.id)) return@precondition

    if (rawInputs.commandName.lowercase() != "configure")
        fail("You must first use the `Configure` command in this guild.")
}