package me.moeszyslak.logbot.services

import com.gitlab.kordlib.core.entity.Attachment
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.Channel
import com.google.common.collect.EvictingQueue
import me.jakejmattson.discordkt.api.annotations.Service
import java.time.Instant

data class CachedMessage(
        val content: String,
        val channel: Channel,
        val user: User,
        val messageId: Long,
        val timestamp: Instant,
        val attachments: Set<Attachment>
)

@Suppress("UnstableApiUsage")
@Service
class CacheService {

    private val messageCaches: MutableMap<Long, EvictingQueue<CachedMessage>> = mutableMapOf()

    fun addMessageToCache(guildId: Long, cachedMessage: CachedMessage) {
        if (!messageCaches.containsKey(guildId)) {
            val cacheAmt = (System.getenv("CACHE_AMT") ?: "4000").toInt()
            messageCaches[guildId] = EvictingQueue.create(cacheAmt)
        }

        messageCaches[guildId]!!.add(cachedMessage)
    }

    fun getMessageFromCache(guildId: Long, messageId: Long): CachedMessage? {
        return messageCaches[guildId]?.firstOrNull { it.messageId == messageId }
    }

    fun removeMessageFromCache(guildId: Long, messageId: Long, ) {
        messageCaches[guildId]?.removeIf { it.messageId == messageId }
    }
}