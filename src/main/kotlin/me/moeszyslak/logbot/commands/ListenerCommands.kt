package me.moeszyslak.logbot.commands

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.kordx.emoji.Emojis
import me.jakejmattson.discordkt.api.arguments.ChoiceArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.moeszyslak.logbot.arguments.ListenerArg
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.extensions.requiredPermissionLevel
import me.moeszyslak.logbot.services.PermissionLevel
import java.awt.Color

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

    guildCommand("IgnoreList") {
        description = "List ignored roles and add/remove roles from the exclusion list"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(ChoiceArg("add/remove/list", "add", "remove", "list").makeOptional("list"),
                RoleArg.makeNullableOptional(null)) {

            val (choice, role) = args
            val config = configuration[(guild.id.longValue)] ?: return@execute

            when (choice) {
                "add" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (config.ignoredRoles.contains(role.id.longValue)) {
                        respond("${role.name} is already being ignored")
                        return@execute
                    }

                    config.ignoredRoles.add(role.id.longValue)
                    configuration.save()

                    respond("${role.name} added to the ignore list")
                }

                "remove" -> {

                    if (role == null) {
                        respond("Received less arguments than expected. Expected: `(Role)`")
                        return@execute
                    }

                    if (!config.ignoredRoles.contains(role.id.longValue)) {
                        respond("${role.name} is not being ignored")
                        return@execute
                    }

                    config.ignoredRoles.remove(role.id.longValue)
                    configuration.save()

                    respond("${role.name} removed from the ignore list")
                }

                "list" -> {
                    respond {
                        title = "Currently ignored roles"

                        if (config.ignoredRoles.isEmpty()) {
                            color = Color(0xE10015)
                            field {
                                value = "There are currently no ignored roles."
                            }
                        } else {
                            color = Color(0xDB5F96)
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