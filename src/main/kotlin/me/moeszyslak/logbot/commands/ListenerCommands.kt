package me.moeszyslak.logbot.commands

import dev.kord.common.entity.Snowflake
import dev.kord.common.kColor
import dev.kord.x.emoji.Emojis
import me.jakejmattson.discordkt.api.arguments.ChoiceArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.moeszyslak.logbot.arguments.ListenerArg
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.dataclasses.Permissions
import java.awt.Color

fun listenerCommands(configuration: Configuration) = commands("Listeners") {
    guildCommand("Status") {
        description = "List all listeners and their current status."
        requiredPermission = Permissions.STAFF
        execute {
            val guildConfig = configuration[guild.id.value] ?: return@execute

            respond {
                title = "Listener status"

                field {
                    value = enumValues<Listener>()
                            .map { if (guildConfig.listenerEnabled(it)) "${Emojis.whiteCheckMark} ${it.name}" else "${Emojis.x} ${it.name}" }
                            .joinToString("\n\n")
                }

            }
        }
    }

    guildCommand("Toggle") {
        description = "Toggle listener"
        requiredPermission = Permissions.STAFF
        execute(ListenerArg) {
            val listener = args.first

            val guildConfig = configuration[guild.id.value] ?: return@execute

            guildConfig.listeners[listener] = !guildConfig.listenerEnabled(listener)
            configuration.save()

            respond("Logging of ${listener.value} is now ${if (guildConfig.listenerEnabled(listener)) "enabled" else "disabled"}")

        }
    }

    guildCommand("IgnoreList") {
        description = "List ignored roles and add/remove roles from the exclusion list"
        requiredPermission = Permissions.STAFF
        execute(ChoiceArg("add/remove/list", "add", "remove", "list").optional("list"),
                RoleArg.optionalNullable(null)) {

            val (choice, role) = args
            val config = configuration[(guild.id.value)] ?: return@execute

            when (choice) {
                "add" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (config.ignoredRoles.contains(role.id.value)) {
                        respond("${role.name} is already being ignored")
                        return@execute
                    }

                    config.ignoredRoles.add(role.id.value)
                    configuration.save()

                    respond("${role.name} added to the ignore list")
                }

                "remove" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (!config.ignoredRoles.contains(role.id.value)) {
                        respond("${role.name} is not being ignored")
                        return@execute
                    }

                    config.ignoredRoles.remove(role.id.value)
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
                                guild.getRole(Snowflake(ignoredRole)).mention
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
