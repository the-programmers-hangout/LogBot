package me.moeszyslak.logbot.conversations

import dev.kord.core.entity.Guild
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.conversations.conversation
import me.moeszyslak.logbot.dataclasses.Configuration

class ConfigurationConversation(private val configuration: Configuration) {
    fun createConfigurationConversation(guild: Guild) = conversation {
        val prefix = prompt(EveryArg, "Bot prefix:")
        val logChannel = prompt(ChannelArg, "Logging channel:")
        val historyChannel = prompt(ChannelArg, "History channel:")

        configuration.setup(guild, prefix, logChannel, historyChannel)
    }
}