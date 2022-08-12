package me.moeszyslak.logbot

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.*
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.LoggerService
import java.awt.Color
import java.time.Instant

@PrivilegedIntent
@KordPreview
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null

    bot(token) {
        val configuration = data("config/config.json") { Configuration() }

        prefix {
            "/"
        }

        configure {
            theme = Color.MAGENTA
            commandReaction = null
            defaultPermissions = Permissions(Permission.ManageMessages)
            intents = Intent.GuildMembers + Intent.GuildVoiceStates + Intent.GuildMessageReactions + Intent.DirectMessagesReactions
        }

        onStart {
            val loggerService = this.getInjectionObjects(LoggerService::class)
            loggerService.logDaemon()
        }
    }
}
