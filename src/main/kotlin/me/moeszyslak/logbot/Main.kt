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
import me.moeszyslak.logbot.services.DiscordCacheService
import java.awt.Color
import java.time.Instant

private val startup = Instant.now()

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
            entitySupplyStrategy = EntitySupplyStrategy.cacheWithRestFallback
            defaultPermissions = Permissions(Permission.ManageMessages)
            intents = Intent.GuildMembers + Intent.GuildVoiceStates + Intent.GuildMessageReactions + Intent.DirectMessagesReactions
        }

        mentionEmbed {
            val guildConfiguration = configuration[it.guild!!.id]

            title = "LogBot 1.4.2"
            description = "A multi-guild discord bot to log everything and everything you could ever want"
            color = it.discord.configuration.theme
            thumbnail(it.discord.kord.getSelf().pfpUrl)
            addInlineField("Source", "[GitHub](https://github.com/the-programmers-hangout/LogBot)")
            addInlineField("Ping", it.discord.kord.gateway.averagePing?.toString() ?: "Unknown")
            addInlineField("Startup", TimeStamp.at(startup, TimeStyle.RELATIVE))
            addInlineField("Logging", "<#${guildConfiguration?.logChannel}>")
            addInlineField("History", "<#${guildConfiguration?.historyChannel}>")
            footer(it.discord.versions.toString())
        }

        onStart {
            val cacheService = this.getInjectionObjects(DiscordCacheService::class)
            try {
                cacheService.run()
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}
