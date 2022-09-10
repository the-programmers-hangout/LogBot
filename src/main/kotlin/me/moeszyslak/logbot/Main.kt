package me.moeszyslak.logbot

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.plus
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.LoggerService
import java.awt.Color

@PrivilegedIntent
@KordPreview
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null

    bot(token) {
        data("config/config.json") { Configuration() }

        prefix { "/" }

        configure {
            theme = Color.MAGENTA
            commandReaction = null
            recommendCommands = false
            defaultPermissions = Permissions(Permission.ManageMessages)
            intents = Intent.GuildMembers + Intent.GuildVoiceStates + Intent.GuildMessageReactions + Intent.DirectMessagesReactions
        }

        onStart {
            val loggerService = this.getInjectionObjects(LoggerService::class)
            loggerService.logDaemon()
        }
    }
}
