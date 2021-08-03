package me.moeszyslak.logbot.dataclasses

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import me.jakejmattson.discordkt.api.dsl.Data

data class Configuration(
        val botOwner: Long = 345541952500006912,
        val guildConfigurations: MutableMap<Long, GuildConfiguration> = mutableMapOf()) : Data("config/config.json") {

    operator fun get(id: Long) = guildConfigurations[id]

    fun hasGuildConfig(guildId: Long) = guildConfigurations.containsKey(guildId)

    fun setup(guild: Guild, prefix: String, adminRole: Role,
              staffRole: Role, logChannel: Channel, historyChannel: Channel) {

        if (guildConfigurations[guild.id.value] != null) return

        val newConfiguration = GuildConfiguration(
                prefix,
                adminRole.id.value,
                staffRole.id.value,
                logChannel.id.value,
                historyChannel.id.value
        )

        guildConfigurations[guild.id.value] = newConfiguration
        save()
    }
}

data class GuildConfiguration(
        var prefix: String,
        var adminRole: Long,
        var staffRole: Long,
        var logChannel: Long,
        var historyChannel: Long,
        var listeners: MutableMap<Listener, Boolean> = mutableMapOf(),
        var ignoredRoles: MutableList<Long> = mutableListOf()) {

    fun listenerEnabled(l: Listener) = listeners[l] ?: false
}

enum class Listener(val value: String) {
    Members("members"),
    Messages("messages"),
    Voice("voice")
}
