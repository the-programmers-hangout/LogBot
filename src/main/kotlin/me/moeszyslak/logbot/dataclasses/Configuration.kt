package me.moeszyslak.logbot.dataclasses

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.channel.Channel
import me.jakejmattson.discordkt.api.dsl.Data

data class Configuration(
        val botOwner: Long = 345541952500006912,
        val guildConfigurations: MutableMap<Long, GuildConfiguration> = mutableMapOf()) : Data("config/config.json") {

    operator fun get(id: Long) = guildConfigurations[id]

    fun hasGuildConfig(guildId: Long) = guildConfigurations.containsKey(guildId)

    fun setup(guild: Guild, prefix: String, adminRole: Role,
              staffRole: Role, logChannel: Channel, historyChannel: Channel) {

        if (guildConfigurations[guild.id.longValue] != null) return

        val newConfiguration = GuildConfiguration(
                prefix,
                adminRole.id.longValue,
                staffRole.id.longValue,
                logChannel.id.longValue,
                historyChannel.id.longValue
        )

        guildConfigurations[guild.id.longValue] = newConfiguration
        save()
    }
}

data class GuildConfiguration(
        var prefix: String,
        var adminRole: Long,
        var staffRole: Long,
        var logChannel: Long,
        var historyChannel: Long,
        var trackMembers: Boolean = true,
        var trackMessages: Boolean = true,
        var listeners: MutableMap<Listener, Boolean> = mutableMapOf(
                Listener.Members to false,
                Listener.Messages to false
        ),
        var ignoredRoles: MutableList<Long> = mutableListOf()
)

enum class Listener(val value: String) {
    Members("members"),
    Messages("messages")
}