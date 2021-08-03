package me.moeszyslak.logbot.conversations

import dev.kord.core.entity.Guild
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.moeszyslak.logbot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.conversations.conversation

class ConfigurationConversation(private val configuration: Configuration) {
    fun createConfigurationConversation(guild: Guild) = conversation {
        val prefix = promptMessage(EveryArg, "Bot prefix:")
        val adminRole = promptMessage(RoleArg, "Admin role:")
        val staffRole = promptMessage(RoleArg, "Staff role:")
        val logChannel = promptMessage(ChannelArg, "Logging channel:")
        val historyChannel = promptMessage(ChannelArg, "History channel:")

        configuration.setup(guild, prefix, adminRole, staffRole, logChannel, historyChannel)
    }
}