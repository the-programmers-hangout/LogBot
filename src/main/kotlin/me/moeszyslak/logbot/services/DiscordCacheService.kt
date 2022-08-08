package me.moeszyslak.logbot.services

import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Configuration

@Service
class DiscordCacheService(private val discord: Discord, private val configuration: Configuration) {
    suspend fun run() {
        configuration.guildConfigurations.forEach { config ->
            try {
                val guild = config.key.let { discord.kord.getGuild(it) } ?: return@forEach
                val roles = guild.withStrategy(EntitySupplyStrategy.cachingRest).roles.toList()
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}