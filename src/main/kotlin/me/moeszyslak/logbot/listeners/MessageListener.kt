package me.moeszyslak.logbot.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.dsl.listeners
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.dataclasses.Listener
import me.moeszyslak.logbot.embeds.createMessageDeleteEmbed
import me.moeszyslak.logbot.embeds.createMessageEditedEmbed
import me.moeszyslak.logbot.services.CacheService
import me.moeszyslak.logbot.services.CachedMessage
import java.time.Instant

@DelicateCoroutinesApi
fun messageListener(configuration: Configuration, cacheService: CacheService, discord: Discord) = listeners {
    on<MessageCreateEvent> {
        message.author?.takeUnless { it.isBot } ?: return@on
        val guild = getGuild() ?: return@on
        val guildConfig = configuration[guild.id] ?: return@on

        val prefix = guildConfig.prefix
        if (message.content.startsWith(prefix)) return@on
        if (message.content.isEmpty()) return@on

        if (!guildConfig.listenerEnabled(Listener.Messages)) return@on

        val author = message.author ?: return@on
        val member = author.asMemberOrNull(guild.id) ?: return@on

        if (!shouldBeLogged(member.roles.toList(), guildConfig.ignoredRoles)) return@on

        val cachedMessage = CachedMessage(
            message.content,
            message.getChannel(),
            author,
            message.id,
            guild.id,
            message.timestamp.toJavaInstant(),
            message.attachments
        )

        cacheService.addMessageToCache(guild.id, cachedMessage)
    }

    on<MessageUpdateEvent> {
        val guildId = new.guildId.value ?: return@on
        val guildConfig = configuration[guildId] ?: return@on
        val newContent = new.content.value ?: return@on
        if (newContent.isEmpty()) return@on
        if (!guildConfig.listenerEnabled(Listener.Messages)) return@on

        val guild = discord.kord.getGuild(guildId) ?: return@on
        val member = new.member.value ?: return@on

        if (!shouldBeLogged(member.roles.map { guild.getRole(it) }, guildConfig.ignoredRoles)) return@on

        val cachedMessage = cacheService.getMessageFromCache(guildId, messageId) ?: return@on

        if (cachedMessage.content == newContent) return@on

        cacheService.removeMessageFromCache(guildId, messageId)

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

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel) ?: return@on

        channel.createEmbed {
            createMessageEditedEmbed(newMessage, cachedMessage)
        }
    }

    fun logMessageDelete(kord: Kord, guildId: Snowflake, messageId: Snowflake) {
        val cachedMessage = cacheService.getMessageFromCache(guildId, messageId) ?: return
        val guildConfig = configuration[guildId] ?: return

        if (!guildConfig.listenerEnabled(Listener.Messages)) return

        GlobalScope.launch {
            val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel) ?: return@launch
            channel.createEmbed {
                createMessageDeleteEmbed(cachedMessage)
            }
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

fun shouldBeLogged(roles: List<Role>, ignoredRoles: MutableList<Snowflake>): Boolean {
    return ignoredRoles.intersect(roles.map { it.id }).isEmpty()
}
