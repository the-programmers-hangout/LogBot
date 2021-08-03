package me.moeszyslak.logbot.listeners

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.jumpLink
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.services.LoggerService

fun reactionListener(loggerService: LoggerService, configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = getGuild() ?: return@on
        val member = guild.getMemberOrNull(userId) ?: return@on

        val guildConfig = configuration[guild.id.value] ?: return@on
        if (!guildConfig.listenerEnabled(Listener.Reactions)) return@on

        val jumpUrl = message.asMessage().jumpLink() ?: return@on
        loggerService.reactionAdd(guild, emoji, member, channel, jumpUrl)
    }

    on<ReactionRemoveEvent> {
        val guild = getGuild() ?: return@on
        val member = guild.getMemberOrNull(userId) ?: return@on

        val guildConfig = configuration[guild.id.value] ?: return@on
        if (!guildConfig.listenerEnabled(Listener.Reactions)) return@on

        val jumpUrl = message.asMessage().jumpLink() ?: return@on
        loggerService.reactionRemove(guild, emoji, member, channel, jumpUrl)
    }
}