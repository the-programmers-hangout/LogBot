package me.moeszyslak.logbot.services

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import me.jakejmattson.discordkt.annotations.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data class CachedMessage(
    val content: String,
    val channel: Channel,
    val user: User,
    val messageId: Snowflake,
    val guildId: Snowflake,
    val timestamp: Instant,
    val attachments: Set<Attachment>
)

@Service
class CacheService {

    private val messages: ConcurrentMap<Snowflake, Cache<Snowflake, CachedMessage>> = ConcurrentHashMap()
    private val cacheAmt = System.getenv("CACHE_AMT")?.toLong() ?: 4000

    fun addMessageToCache(guildId: Snowflake, cachedMessage: CachedMessage) {
        val cache = messages.getOrPut(guildId) {
            CacheBuilder.newBuilder().maximumSize(cacheAmt).build()
        }

        cache.put(cachedMessage.messageId, cachedMessage)
    }

    fun getMessageFromCache(guildId: Snowflake, messageId: Snowflake): CachedMessage? {
        return messages[guildId]?.getIfPresent(messageId)
    }

    fun removeMessageFromCache(guildId: Snowflake, messageId: Snowflake) {
        messages[guildId]?.invalidate(messageId)
    }
}
