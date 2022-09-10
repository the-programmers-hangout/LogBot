package me.moeszyslak.logbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.kColor
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.dsl.edit
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import java.awt.Color

fun listenerCommands(configuration: Configuration) = subcommand("listener", Permissions(Permission.ManageMessages)) {
    sub("Status", "List all listeners and their status.") {
        execute {
            val guildConfig = configuration[guild.id] ?: return@execute

            respondPublic {
                field {
                    name = "Listener status"
                    value = enumValues<Listener>().joinToString("\n\n") {
                        (if (guildConfig.listenerEnabled(it)) Emojis.whiteCheckMark else Emojis.x).toString() + " ${it.name}"
                    }
                }
            }
        }
    }

    sub("Toggle", "Toggle a listener on or off.") {
        execute(ChoiceArg("Listener", "The listener to toggle", *Listener.values())) {
            val listener = args.first
            val guildConfig = configuration[guild.id] ?: return@execute

            configuration.edit { guildConfig.listeners[listener] = !guildConfig.listenerEnabled(listener) }

            respondPublic("Logging of ${listener.value} is now ${if (guildConfig.listenerEnabled(listener)) "enabled" else "disabled"}")
        }
    }
}