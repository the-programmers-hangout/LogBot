package me.moeszyslak.logbot.services

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import me.jakejmattson.discordkt.api.annotations.Service
import java.time.Instant
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

data class CachedMessage(
    val content: String,
    val channel: Channel,
    val user: User,
    val messageId: Long,
    val guildId: Long,
    val timestamp: Instant,
    val attachments: Set<Attachment>
)

@Service
class CacheService {

    private val messages: ConcurrentMap<Long, Cache<Long, CachedMessage>> = ConcurrentHashMap()
    private val cacheAmt = System.getenv("CACHE_AMT")?.toLong() ?: 4000

    fun addMessageToCache(guildId: Long, cachedMessage: CachedMessage) {
        val cache = messages.getOrPut(guildId) {
            CacheBuilder.newBuilder().maximumSize(cacheAmt).build()
        }

        cache.put(cachedMessage.messageId, cachedMessage)
    }

    fun getMessageFromCache(guildId: Long, messageId: Long): CachedMessage? {
        return messages[guildId]?.getIfPresent(messageId)
    }

    fun removeMessageFromCache(guildId: Long, messageId: Long) {
        messages[guildId]?.invalidate(messageId)
    }
}
