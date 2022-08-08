package me.moeszyslak.logbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.commands.commands
import me.moeszyslak.logbot.conversations.ConfigurationConversation
import me.moeszyslak.logbot.dataclasses.Configuration

fun guildConfigCommands(configuration: Configuration) = commands("Configuration", Permissions(Permission.Administrator)) {
    text("Setup") {
        description = "Set up a guild to use this bot."
        execute {
            if (configuration.hasGuildConfig(guild.id)) {
                respond("Guild configuration exists. To modify it use the commands to set values.")
                return@execute
            }
            ConfigurationConversation(configuration)
                .createConfigurationConversation(guild)
                .startPublicly(discord, author, channel)

            respond("${guild.name} has been setup")
        }
    }

    text("Prefix") {
        description = "Set the bot prefix."
        execute(EveryArg) {
            if (!configuration.hasGuildConfig(guild.id)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val prefix = args.first
            configuration[guild.id]?.prefix = prefix
            configuration.save()
            respond("Prefix set to: **$prefix**")
        }
    }
}