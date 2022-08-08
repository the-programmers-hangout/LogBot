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
    text("Status") {
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

    text("Toggle") {
        description = "Toggle listener"
        execute(ChoiceArg("Listener", "The listener to toggle", *Listener.values())) {
            val listener = args.first

            val guildConfig = configuration[guild.id] ?: return@execute

            guildConfig.listeners[listener] = !guildConfig.listenerEnabled(listener)
            configuration.save()

            respond("Logging of ${listener.value} is now ${if (guildConfig.listenerEnabled(listener)) "enabled" else "disabled"}")

        }
    }

    text("IgnoreList") {
        description = "List ignored roles and add/remove roles from the exclusion list"
        execute(ChoiceArg("add/remove/list", "add", "remove", "list").optional("list"),
                RoleArg.optionalNullable(null)) {

            val (choice, role) = args
            val config = configuration[guild.id] ?: return@execute

            when (choice) {
                "add" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (config.ignoredRoles.contains(role.id)) {
                        respond("${role.name} is already being ignored")
                        return@execute
                    }

                    config.ignoredRoles.add(role.id)
                    configuration.save()

                    respond("${role.name} added to the ignore list")
                }

                "remove" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (!config.ignoredRoles.contains(role.id)) {
                        respond("${role.name} is not being ignored")
                        return@execute
                    }

                    config.ignoredRoles.remove(role.id)
                    configuration.save()

                    respond("${role.name} removed from the ignore list")
                }

                "list" -> {
                    respond {
                        title = "Currently ignored roles"

                        if (config.ignoredRoles.isEmpty()) {
                            color = Color(0xE10015).kColor
                            field {
                                value = "There are currently no ignored roles."
                            }
                        } else {
                            color = Color(0xDB5F96).kColor
                            val roles = config.ignoredRoles.map { ignoredRole ->
                                guild.getRole(ignoredRole).mention
                            }

                            field {
                                value = roles.joinToString("\n")
                            }
                        }

                    }
                }

                else -> {
                    respond("Invalid choice")
                }
            }
        }
    }
}
