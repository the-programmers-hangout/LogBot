package me.moeszyslak.logbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.kColor
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import java.awt.Color

fun listenerCommands(configuration: Configuration) = commands("Listeners", Permissions(Permission.ManageMessages)) {
    slash("Status") {
        description = "List all listeners and their current status."
        execute {
            val guildConfig = configuration[guild.id] ?: return@execute

            respond {
                title = "Listener status"

                field {
                    value =
                        enumValues<Listener>().joinToString("\n\n") { if (guildConfig.listenerEnabled(it)) "${Emojis.whiteCheckMark} ${it.name}" else "${Emojis.x} ${it.name}" }
                }
            }
        }
    }

    slash("Toggle") {
        description = "Toggle listener"
        execute(ChoiceArg("Listener", "The listener to toggle", *Listener.values())) {
            val listener = args.first
            val guildConfig = configuration[guild.id] ?: return@execute

            guildConfig.listeners[listener] = !guildConfig.listenerEnabled(listener)
            configuration.save()

            respond("Logging of ${listener.value} is now ${if (guildConfig.listenerEnabled(listener)) "enabled" else "disabled"}")
        }
    }
}