package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.services.LoggerService

fun memberListener(loggerService: LoggerService, configuration: Configuration) = listeners {
    on<MemberJoinEvent> {
        val guild = getGuild()
        val guildConfig = configuration[guild.id.longValue] ?: return@on

        if (!guildConfig.listenerEnabled(Listener.Members)) return@on

        loggerService.memberJoin(guild, member)
    }

    on<MemberLeaveEvent> {
        val guild = getGuild()
        val guildConfig = configuration[guild.id.longValue] ?: return@on

        if (!guildConfig.listenerEnabled(Listener.Members)) return@on

        loggerService.memberLeave(guild, user)
    }
}
