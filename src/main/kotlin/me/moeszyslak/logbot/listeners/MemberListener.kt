package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import com.gitlab.kordlib.core.event.guild.MemberLeaveEvent
import me.jakejmattson.discordkt.api.dsl.listeners
import me.moeszyslak.logbot.services.LoggerService

fun memberListener(loggerService: LoggerService) = listeners {
    on<MemberJoinEvent> {
        loggerService.memberJoin(getGuild(), member)
    }

    on<MemberLeaveEvent> {
        loggerService.memberLeave(getGuild(), user)
    }
}