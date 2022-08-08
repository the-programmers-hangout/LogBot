package me.moeszyslak.logbot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import me.jakejmattson.discordkt.dsl.Data

data class Configuration(val botOwner: Long = 345541952500006912,
                         val guildConfigurations: MutableMap<Snowflake, GuildConfiguration> = mutableMapOf()) : Data() {

    operator fun get(id: Snowflake) = guildConfigurations[id]

    fun hasGuildConfig(guildId: Snowflake) = guildConfigurations.containsKey(guildId)

    fun setup(guild: Guild, prefix: String, adminRole: Role,
              staffRole: Role, logChannel: Channel, historyChannel: Channel) {

        if (guildConfigurations[guild.id] != null) return

        val newConfiguration = GuildConfiguration(prefix, adminRole.id, staffRole.id, logChannel.id, historyChannel.id)

        guildConfigurations[guild.id] = newConfiguration
        save()
    }
}

data class GuildConfiguration(
    var prefix: String,
    var adminRole: Snowflake,
    var staffRole: Snowflake,
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
