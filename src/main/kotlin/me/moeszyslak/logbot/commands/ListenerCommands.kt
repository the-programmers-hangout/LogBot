package me.moeszyslak.logbot.commands

import com.gitlab.kordlib.kordx.emoji.Emojis
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.arguments.ListenerArg
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.extensions.descriptor
import me.moeszyslak.logbot.extensions.requiredPermissionLevel
import me.moeszyslak.logbot.services.PermissionLevel
import java.time.LocalDateTime
import java.time.ZoneOffset

fun listenerCommands(configuration: Configuration) = commands("Listeners") {
    guildCommand("Status") {
        description = "List all listeners and their current status."
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            val guildConfig = configuration[guild.id.longValue] ?: return@execute

            respond {
                title = "Listener status"

                guildConfig.listeners
                        .map { if (it.value) "${Emojis.whiteCheckMark} ${it.key.name}" else "${Emojis.x} ${it.key.name}" }
                        .forEach {
                            field {
                                name = it
                            }
                        }
            }
        }
    }

    guildCommand("Toggle") {
        description = "Toggle listener"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ListenerArg) {
            val listener = args.first

            val guildConfig = configuration[guild.id.longValue] ?: return@execute

            guildConfig.listeners[listener] = !guildConfig.listeners[listener]!!
            configuration.save()

            respond("${listener.value} is now ${if (guildConfig.listeners[listener]!!) "enabled" else "disabled"}")

        }
    }
}