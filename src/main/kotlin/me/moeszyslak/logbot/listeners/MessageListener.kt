package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.channel.TextChannel
import com.gitlab.kordlib.core.event.message.MessageBulkDeleteEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.event.message.MessageDeleteEvent
import com.gitlab.kordlib.core.event.message.MessageUpdateEvent
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.embeds.createMessageDeleteEmbed
import me.moeszyslak.logbot.embeds.createMessageEditedEmbed
import me.moeszyslak.logbot.services.CacheService
import me.moeszyslak.logbot.services.CachedMessage
import java.time.Instant

fun messageListener(configuration: Configuration, cacheService: CacheService, discord: Discord) = listeners {
    on<MessageCreateEvent> {
        message.author!!.takeUnless { it.isBot == true } ?: return@on
        val guild = getGuild() ?: return@on
        val guildConfig = configuration[guild.id.longValue] ?: return@on

        val prefix = guildConfig.prefix
        if (message.content.startsWith(prefix)) return@on

        if (!guildConfig.listeners[Listener.Messages]!!) return@on

        val author = message.author ?: return@on
        val member = author.asMember(guild.id)
        if (!shouldBeLogged(member.roles.toList(), guildConfig.ignoredRoles)) return@on

        val cachedMessage = CachedMessage(
                message.content,
                message.getChannel(),
                author,
                message.id.longValue,
                guild.id.longValue,
                message.timestamp,
                message.attachments
        )

        cacheService.addMessageToCache(guild.id.longValue, cachedMessage)
    }

    on<MessageUpdateEvent> {
        new.author!!.takeUnless { it.bot == true } ?: return@on
        val guildId = new.guildId?.toLongOrNull() ?: return@on
        val guildConfig = configuration[guildId] ?: return@on
        if (!guildConfig.listeners[Listener.Messages]!!) return@on

        val guild = discord.api.getGuild(guildId.toSnowflake()) ?: return@on
        val member = new.member ?: return@on
        val roles = member.roles.mapNotNull { guild.getRoleOrNull(it.toSnowflake()) }
        if (!shouldBeLogged(roles, guildConfig.ignoredRoles)) return@on

        val cachedMessage = cacheService.getMessageFromCache(guildId, messageId.longValue) ?: return@on
        val newContent = new.content ?: return@on
        if (cachedMessage.content == newContent) return@on


        cacheService.removeMessageFromCache(guildId, messageId.longValue)

        val newMessage = CachedMessage(
                newContent,
                cachedMessage.channel,
                cachedMessage.user,
                cachedMessage.messageId,
                guildId,
                Instant.now(),
                cachedMessage.attachments


        )
        cacheService.addMessageToCache(guildId, newMessage)

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return@on


        channel.createEmbed {
            createMessageEditedEmbed(newMessage, cachedMessage)
        }

    }

    suspend fun logMessageDelete(kord: Kord, guildId: Snowflake, messageId: Snowflake): Unit {
        val cachedMessage = cacheService.getMessageFromCache(guildId.longValue, messageId.longValue) ?: return
        val guildConfig = configuration[guildId.longValue] ?: return

        if (!guildConfig.listeners[Listener.Messages]!!) return
        val roles = cachedMessage.user.asMember(guildId).roles.toList()
        if (!shouldBeLogged(roles, guildConfig.ignoredRoles)) return

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return
        channel.createEmbed {
            createMessageDeleteEmbed(cachedMessage)
        }
    }

    on<MessageDeleteEvent> {
        val guild = getGuild() ?: return@on
        logMessageDelete(kord, guild.id, messageId)
    }

    on<MessageBulkDeleteEvent> {
        val guild = getGuild() ?: return@on
        messageIds.forEach { logMessageDelete(kord, guild.id, it) }
    }
}

fun shouldBeLogged(roles: List<Role>, ignoredRoles: MutableList<Long>): Boolean {
    return ignoredRoles.intersect(roles.map { it.id.longValue }).isEmpty()
}
