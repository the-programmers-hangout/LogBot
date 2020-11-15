package me.moeszyslak.logbot.listeners

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.channel.TextChannel
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
                Instant.now(),
                cachedMessage.attachments


        )
        cacheService.addMessageToCache(guildId, newMessage)

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return@on


        channel.createEmbed {
            createMessageEditedEmbed(newMessage, cachedMessage)
        }

    }

    on<MessageDeleteEvent> {
        val guild = getGuild() ?: return@on
        val cachedMessage = cacheService.getMessageFromCache(guild.id.longValue, messageId.longValue) ?: return@on
        val guildConfig = configuration[guild.id.longValue] ?: return@on

        if (!guildConfig.listeners[Listener.Messages]!!) return@on
        val roles = cachedMessage.user.asMember(guild.id).roles.toList()
        if (!shouldBeLogged(roles, guildConfig.ignoredRoles)) return@on

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return@on
        channel.createEmbed {
            createMessageDeleteEmbed(cachedMessage)
        }
    }
}

fun shouldBeLogged(roles: List<Role>, ignoredRoles: MutableList<Long>): Boolean {
    return ignoredRoles.intersect(roles.map { it.id.longValue }).isEmpty()
}