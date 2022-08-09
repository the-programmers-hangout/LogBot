package me.moeszyslak.logbot.commands

import dev.kord.common.kColor
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.subcommand
import me.moeszyslak.logbot.dataclasses.Configuration
import java.awt.Color

fun ignore(configuration: Configuration) = subcommand("Ignored") {
    sub("Add") {
        description = "Ignore this role."
        execute(RoleArg("Role", "Role to start ignoring.")) {
            val role = args.first
            val config = configuration[guild.id] ?: return@execute

            if (config.ignoredRoles.contains(role.id)) {
                respond("${role.name} is already being ignored")
                return@execute
            }

            config.ignoredRoles.add(role.id)
            configuration.save()

            respond("${role.name} added to the ignore list")
        }
    }

    sub("Remove") {
        description = "Stop ignoring this role."
        execute(RoleArg("Role", "Role to stop ignoring.")) {
            val role = args.first
            val config = configuration[guild.id] ?: return@execute

            if (!config.ignoredRoles.contains(role.id)) {
                respond("${role.name} is not being ignored")
                return@execute
            }

            config.ignoredRoles.remove(role.id)
            configuration.save()

            respond("${role.name} removed from the ignore list")
        }
    }

    sub("List") {
        description = "List ignored roles."
        execute {
            val config = configuration[guild.id] ?: return@execute

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
    }
}