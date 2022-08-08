package me.moeszyslak.logbot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

@Serializable
data class Configuration(val guildConfigurations: MutableMap<Snowflake, GuildConfiguration> = mutableMapOf()) : Data() {

    operator fun get(id: Snowflake) = guildConfigurations[id]

    fun hasGuildConfig(guildId: Snowflake) = guildConfigurations.containsKey(guildId)

    fun setup(guild: Guild, prefix: String, logChannel: Channel, historyChannel: Channel) {
        if (guildConfigurations[guild.id] != null) return
        guildConfigurations[guild.id] = GuildConfiguration(prefix, logChannel.id, historyChannel.id)
        save()
    }
}

@Serializable
data class GuildConfiguration(
    var prefix: String,
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
