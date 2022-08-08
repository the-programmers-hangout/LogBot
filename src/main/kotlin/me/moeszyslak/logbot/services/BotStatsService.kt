package me.moeszyslak.logbot.services

import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.util.timeToString
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import java.util.*

@Service
class BotStatsService(private val configuration: Configuration, private val discord: Discord) {
    private var startTime: Date = Date()

    val uptime: String
        get() = timeToString(Date().time - startTime.time)

    val ping: String
        get() = "${discord.kord.gateway.averagePing}"
}