package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.VoiceStateUpdateEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.embeds.createVoiceJoinEmbed
import me.moeszyslak.logbot.embeds.createVoiceLeaveEmbed

fun voiceListener(configuration: Configuration) = listeners {
    on<VoiceStateUpdateEvent> {
        val guildId = state.guildId ?: return@on
        val guildConfig = configuration[guildId.longValue] ?: return@on

        val channel = kord.getChannelOf<TextChannel>(guildConfig.logChannel.toSnowflake())
            ?: return@on

        if (state.channelId == null) {
            val left = old ?: return@on
            val channelId = left.channelId ?: return@on

            channel.createEmbed {
                createVoiceLeaveEmbed(left.getMember(), channelId)
            }
        } else if (state.channelId != old?.channelId) {
            val channelId = state.channelId ?: return@on

            channel.createEmbed {
                createVoiceJoinEmbed(state.getMember(), channelId)
            }
        }
    }
}
