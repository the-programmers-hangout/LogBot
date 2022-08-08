package me.moeszyslak.logbot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

@Serializable
data class Configuration(val guildConfigurations: MutableMap<Snowflake, GuildConfiguration> = mutableMapOf()) : Data() {

    operator fun get(id: Snowflake) = guildConfigurations[id]

    operator fun set(id: Snowflake, configuration: GuildConfiguration) {
        guildConfigurations[id] = configuration
        save()
    }

    fun hasGuildConfig(guildId: Snowflake) = guildConfigurations.containsKey(guildId)
}

@Serializable
data class GuildConfiguration(
    var logChannel: Snowflake,
    var historyChannel: Snowflake,
    var listeners: MutableMap<Listener, Boolean> = mutableMapOf(),
    var ignoredRoles: MutableList<Snowflake> = mutableListOf()) {

    fun listenerEnabled(l: Listener) = listeners[l] ?: false
}

enum class Listener(val value: String) {
    Members("members"),
    Messages("messages"),
    Voice("voice"),
    Reactions("reactions")
}
