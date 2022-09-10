package me.moeszyslak.logbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.commands.commands
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.GuildConfiguration

fun guildConfigCommands(configuration: Configuration) = commands("Configuration", Permissions(Permission.Administrator)) {
    slash("Configure", "Configure a guild.") {
        execute(ChannelArg("Logging", "Logging Channel"), ChannelArg("History", "History Channel")) {
            configuration[guild.id] = GuildConfiguration(args.first.id, args.second.id)
            respond("${guild.name} has been setup")
        }
    }
}