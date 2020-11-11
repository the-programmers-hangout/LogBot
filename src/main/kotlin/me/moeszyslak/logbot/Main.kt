package me.moeszyslak.logbot

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.gateway.Intent
import com.gitlab.kordlib.gateway.Intents
import com.gitlab.kordlib.gateway.PrivilegedIntent
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.services.BotStatsService
import me.moeszyslak.logbot.services.PermissionsService
import me.jakejmattson.discordkt.api.dsl.bot
import me.moeszyslak.logbot.extensions.requiredPermissionLevel
import java.awt.Color

@PrivilegedIntent
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null
    val prefix = System.getenv("DEFAULT_PREFIX") ?: "<none>"

    require(token != null) { "Expected the bot token as an environment variable" }

    bot(token) {
        prefix {
            val configuration = discord.getInjectionObjects(Configuration::class)

            guild?.let { configuration[it.id.longValue]?.prefix } ?: prefix
        }

        configure {
            theme = Color.MAGENTA
        }

        mentionEmbed {
            val configuration = it.discord.getInjectionObjects(Configuration::class)
            val statsService = it.discord.getInjectionObjects(BotStatsService::class)
            val guildConfiguration = configuration[it.guild!!.id.longValue]

            title = "LogBot"
            description = "A multi-guild discord bot to log everything and everything you could ever want"

            color = it.discord.configuration.theme

            thumbnail {
                url = it.discord.api.getSelf().avatar.url
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
                val adminRole = it.guild!!.getRole(Snowflake(guildConfiguration.adminRole))
                val staffRole = it.guild!!.getRole(Snowflake(guildConfiguration.staffRole))

                val loggingChannel = it.guild!!.getChannel(Snowflake(guildConfiguration.logChannel))
                val historyChannel = it.guild!!.getChannel(Snowflake(guildConfiguration.historyChannel))

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
                        "Version: 1.0.0\n" +
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

        permissions {
            val permissionsService = discord.getInjectionObjects(PermissionsService::class)
            val permission = command.requiredPermissionLevel
            if (guild != null) {
                permissionsService.hasClearance(user.asMember(guild!!.id), permission)
            } else {
                false
            }
        }

        intents {
            Intents.nonPrivileged.intents.forEach {
                +it
            }

            +Intent.GuildMembers
        }
    }
}