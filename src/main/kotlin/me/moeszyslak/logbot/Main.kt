package me.moeszyslak.logbot

import dev.kord.common.annotation.KordPreview
import dev.kord.common.kColor
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.BotStatsService
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Permissions
import me.moeszyslak.logbot.services.DiscordCacheService
import java.awt.Color

@PrivilegedIntent
@KordPreview
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null
    val prefix = System.getenv("DEFAULT_PREFIX") ?: "<none>"

    require(token != null) { "Expected the bot token as an environment variable" }

    bot(token) {
        prefix {
            val configuration = discord.getInjectionObjects(Configuration::class)

            guild?.let { configuration[it.id.value]?.prefix } ?: prefix
        }

        configure {
            theme = Color.MAGENTA
            allowMentionPrefix = true
            commandReaction = null
            entitySupplyStrategy = EntitySupplyStrategy.cacheWithRestFallback
            permissions(Permissions.STAFF)
            intents = Intents.all
        }

        mentionEmbed {
            val configuration = it.discord.getInjectionObjects(Configuration::class)
            val statsService = it.discord.getInjectionObjects(BotStatsService::class)
            val guildConfiguration = configuration[it.guild!!.id.value]

            title = "LogBot"
            description = "A multi-guild discord bot to log everything and everything you could ever want"

            color = it.discord.configuration.theme?.kColor

            thumbnail {
                url = it.channel.kord.getSelf().avatar.url
            }

            field {
                name = "Prefix"
                value = it.prefix()
                inline = true
            }

            field {
                name = "Ping"
                value = statsService.ping
                inline = true
            }

            if (guildConfiguration != null) {
                val adminRole = it.guild!!.getRole(guildConfiguration.adminRole.toSnowflake())
                val staffRole = it.guild!!.getRole(guildConfiguration.staffRole.toSnowflake())

                val loggingChannel = it.guild!!.getChannel(guildConfiguration.logChannel.toSnowflake())
                val historyChannel = it.guild!!.getChannel(guildConfiguration.historyChannel.toSnowflake())

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
                        "Version: 1.4.1\n" +
                        "DiscordKt: ${versions.library}\n" +
                        "Kord: ${versions.kord}\n" +
                        "Kotlin: ${versions.kotlin}" +
                        "```"
            }

            field {
                name = "Uptime"
                value = statsService.uptime
                inline = true
            }

            field {
                name = "Source"
                value = "[GitHub](https://github.com/the-programmers-hangout/LogBot)"
                inline = true
            }
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
