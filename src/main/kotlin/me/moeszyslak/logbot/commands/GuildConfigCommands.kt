package me.moeszyslak.logbot.commands

import me.moeszyslak.logbot.conversations.ConfigurationConversation
import me.moeszyslak.logbot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.commands.commands
import me.moeszyslak.logbot.dataclasses.Permissions

fun guildConfigCommands(configuration: Configuration) = commands("Configuration") {
    guildCommand("Setup") {
        description = "Set up a guild to use this bot."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            if (configuration.hasGuildConfig(guild.id.value)) {
                respond("Guild configuration exists. To modify it use the commands to set values.")
                return@execute
            }
            ConfigurationConversation(configuration)
                    .createConfigurationConversation(guild)
                    .startPublicly(discord, author, channel)

            respond("${guild.name} has been setup")
        }
    }

    guildCommand("Prefix") {
        description = "Set the bot prefix."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(EveryArg) {
            if (!configuration.hasGuildConfig(guild.id.value)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val prefix = args.first
            configuration[guild.id.value]?.prefix = prefix
            configuration.save()
            respond("Prefix set to: **$prefix**")
        }
    }

    guildCommand("StaffRole") {
        description = "Set the bot staff role."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.value)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.value]?.staffRole = role.id.value
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }

    guildCommand("AdminRole") {
        description = "Set the bot admin role."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.value)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.value]?.adminRole = role.id.value
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }
}