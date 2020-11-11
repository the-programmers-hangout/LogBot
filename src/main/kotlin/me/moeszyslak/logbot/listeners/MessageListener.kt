package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import me.jakejmattson.discordkt.api.dsl.listeners

fun messageListener() = listeners {
    on<MessageCreateEvent> {

    }
}