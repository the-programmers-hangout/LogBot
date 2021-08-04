package me.moeszyslak.logbot.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageBulkDeleteEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.MessageUpdateEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
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

@DelicateCoroutinesApi
fun messageListener(configuration: Configuration, cacheService: CacheService, discord: Discord) = listeners {
    on<MessageCreateEvent> {
        message.author!!.takeUnless { it.isBot } ?: return@on
        val guild = getGuild() ?: return@on
        val guildConfig = configuration[guild.id.value] ?: return@on

        val prefix = guildConfig.prefix
        if (message.content.startsWith(prefix)) return@on

        if (!guildConfig.listenerEnabled(Listener.Messages)) return@on

        val author = message.author ?: return@on
        val member = author.asMemberOrNull(guild.id) ?: return@on

        if (!shouldBeLogged(member.roles.toList(), guildConfig.ignoredRoles)) return@on

        val cachedMessage = CachedMessage(
                message.content,
                message.getChannel(),
                author,
                message.id.value,
                guild.id.value,
                message.timestamp.toJavaInstant(),
                message.attachments
        )

        cacheService.addMessageToCache(guild.id.value, cachedMessage)
    }

    on<MessageUpdateEvent> {
        new.author.value.takeUnless { it!!.bot.orElse(false) } ?: return@on
        val guildId = new.guildId.value ?: return@on
        val guildConfig = configuration[guildId.value] ?: return@on
        if (!guildConfig.listenerEnabled(Listener.Messages)) return@on

        val guild = discord.kord.getGuild(guildId) ?: return@on
        val member = new.member.value ?: return@on
        if (!shouldBeLogged(member.roles.map { guild.getRole(it) }, guildConfig.ignoredRoles)) return@on

        val cachedMessage = cacheService.getMessageFromCache(guildId.value, messageId.value) ?: return@on
        val newContent = new.content.value ?: return@on
        if (cachedMessage.content == newContent) return@on


        cacheService.removeMessageFromCache(guildId.value, messageId.value)

        val newMessage = CachedMessage(
                newContent,
                cachedMessage.channel,
                cachedMessage.user,
                cachedMessage.messageId,
                guildId.value,
                Instant.now(),
                cachedMessage.attachments


        )
        cacheService.addMessageToCache(guildId.value, newMessage)

        val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return@on


        channel.createEmbed {
            createMessageEditedEmbed(newMessage, cachedMessage)
        }

    }

    suspend fun logMessageDelete(kord: Kord, guildId: Snowflake, messageId: Snowflake) {
        val cachedMessage = cacheService.getMessageFromCache(guildId.value, messageId.value) ?: return
        val guildConfig = configuration[guildId.value] ?: return

        if (!guildConfig.listenerEnabled(Listener.Messages)) return

        GlobalScope.launch {
            val channel = kord.getChannelOf<TextChannel>(guildConfig.historyChannel.toSnowflake()) ?: return@launch
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

fun shouldBeLogged(roles: List<Role>, ignoredRoles: MutableList<Long>): Boolean {
    return ignoredRoles.intersect(roles.map { it.id.value }).isEmpty()
}
