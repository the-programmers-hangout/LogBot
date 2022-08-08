package me.moeszyslak.logbot

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.addInlineField
import me.jakejmattson.discordkt.extensions.pfpUrl
import me.jakejmattson.discordkt.extensions.thumbnail
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.BotStatsService
import me.moeszyslak.logbot.services.DiscordCacheService
import java.awt.Color

@PrivilegedIntent
@KordPreview
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null
    val prefix = System.getenv("DEFAULT_PREFIX") ?: "<none>"

    require(token != null) { "Expected the bot token as an environment variable" }

    bot(token) {
        val configuration = data("config/config.json") { Configuration() }

        prefix {
            guild?.let { configuration[it.id]?.prefix } ?: prefix
        }

        configure {
            theme = Color.MAGENTA
            mentionAsPrefix = true
            commandReaction = null
            entitySupplyStrategy = EntitySupplyStrategy.cacheWithRestFallback
            defaultPermissions = Permissions(Permission.ManageMessages)
            intents = Intents.all
        }

        mentionEmbed {
            val statsService = it.discord.getInjectionObjects(BotStatsService::class)
            val guildConfiguration = configuration[it.guild!!.id]

            title = "LogBot"
            description = "A multi-guild discord bot to log everything and everything you could ever want"
            color = it.discord.configuration.theme
            thumbnail(it.channel.kord.getSelf().pfpUrl)
            addInlineField("Prefix", it.prefix())
            addInlineField("Ping", statsService.ping)

            if (guildConfiguration != null) {
                val adminRole = it.guild!!.getRole(guildConfiguration.adminRole)
                val staffRole = it.guild!!.getRole(guildConfiguration.staffRole)
                val loggingChannel = it.guild!!.getChannel(guildConfiguration.logChannel)
                val historyChannel = it.guild!!.getChannel(guildConfiguration.historyChannel)

                field {
                    name = "Configuration"
                    value = "```" +
                        "Admin Role: ${adminRole.name}\n" +
                        "Staff Role: ${staffRole.name}\n" +
                        "Logging Channel: ${loggingChannel.name}\n" +
                        "History Channel: ${historyChannel.name}" +
                        "```"
                }
            }

            field {
                val versions = it.discord.versions

                name = "Bot Info"
                value = "```" +
                    "Version: 1.4.2\n" +
                    "DiscordKt: ${versions.library}\n" +
                    "Kord: ${versions.kord}\n" +
                    "Kotlin: ${versions.kotlin}" +
                    "```"
            }

            addInlineField("Uptime", statsService.uptime)
            addInlineField("Source", "[GitHub](https://github.com/the-programmers-hangout/LogBot)")
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
