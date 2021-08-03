package me.moeszyslak.logbot.listeners

import dev.kord.core.event.user.VoiceStateUpdateEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.services.LoggerService

fun voiceListener(loggerService: LoggerService, configuration: Configuration) = listeners {
    on<VoiceStateUpdateEvent> {
        val guild = state.getGuild()

        val guildConfig = configuration[guild.id.value] ?: return@on
        if (!guildConfig.listenerEnabled(Listener.Voice)) return@on

        if (state.channelId == null) {
            val left = old ?: return@on
            val channelId = left.channelId ?: return@on

            loggerService.voiceChannelLeave(guild, left.getMember(), channelId)
        } else if (state.channelId != old?.channelId) {
            val channelId = state.channelId ?: return@on

            loggerService.voiceChannelJoin(guild, state.getMember(), channelId)
        }
    }
}
