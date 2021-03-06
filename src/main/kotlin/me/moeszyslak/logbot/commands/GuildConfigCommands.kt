package me.moeszyslak.logbot.commands

import me.moeszyslak.logbot.conversations.ConfigurationConversation
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.PermissionLevel
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.moeszyslak.logbot.extensions.requiredPermissionLevel

fun guildConfigCommands(configuration: Configuration) = commands("Configuration") {
    guildCommand("Setup") {
        description = "Set up a guild to use this bot."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        execute {
            if (configuration.hasGuildConfig(guild.id.longValue)) {
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
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(EveryArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val prefix = args.first
            configuration[guild.id.longValue]?.prefix = prefix
            configuration.save()
            respond("Prefix set to: **$prefix**")
        }
    }

    guildCommand("StaffRole") {
        description = "Set the bot staff role."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.longValue]?.staffRole = role.id.longValue
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }

    guildCommand("AdminRole") {
        description = "Set the bot admin role."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.longValue]?.adminRole = role.id.longValue
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }
}