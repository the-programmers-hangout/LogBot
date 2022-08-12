package me.moeszyslak.logbot.services

import com.google.common.cache.Cache
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.allowedMentions
import kotlinx.coroutines.*
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.descriptor
import me.jakejmattson.discordkt.extensions.idDescriptor
import me.moeszyslak.logbot.dataclasses.Configuration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data class LogMessage(
    val logMsg: String,
    val shouldPing: Boolean
)

@Service
class LoggerService(private val config: Configuration, private val discord: Discord) {

    private val logMessages: MutableMap<Snowflake, MutableList<LogMessage>> = mutableMapOf()

    private fun addToLog(guild: Guild, message: String, shouldPing: Boolean = true) {
        if (!logMessages.containsKey(guild.id)) {
            logMessages[guild.id] = mutableListOf()
        }

        logMessages[guild.id]!!.add(LogMessage(message, shouldPing))
    }


    /*

        Member Join/Leave Events

     */
    fun memberJoin(guild: Guild, member: Member) {
        addToLog(
            guild, "${member.descriptor()} " +
                    "created at ${LocalDateTime.ofInstant(member.id.timestamp.toJavaInstant(), ZoneOffset.UTC)} " +
                    "joined the server")

    }

    fun memberLeave(guild: Guild, user: User) {
        addToLog(guild, "${user.descriptor()} " +
                "created at ${LocalDateTime.ofInstant(user.id.timestamp.toJavaInstant(), ZoneOffset.UTC)} " +
                "left the server")
    }

    /*

        Voice Channel Events


     */
    fun voiceChannelJoin(guild: Guild, user: User, channelId: Snowflake) {
        addToLog(guild, "${user.idDescriptor()} joined voice channel <#${channelId.value}>", false)

    }

    fun voiceChannelLeave(guild: Guild, user: User, channelId: Snowflake) {
        addToLog(guild, "${user.idDescriptor()} left voice channel <#${channelId.value}>", false)
    }

    /*

        Reaction Events

     */

    fun reactionAdd(guild: Guild, reaction: ReactionEmoji, member: Member, channel: MessageChannelBehavior, jumpUrl: String) {
        addToLog(guild, "${member.idDescriptor()} " +
                "added reaction ${reaction.mention} in ${channel.mention} :: $jumpUrl", false)
    }

    fun reactionRemove(guild: Guild, reaction: ReactionEmoji, member: Member, channel: MessageChannelBehavior, jumpUrl: String) {
        addToLog(guild, "${member.idDescriptor()} " +
                "removed reaction ${reaction.mention} in ${channel.mention} :: $jumpUrl", false)
    }




    @OptIn(DelicateCoroutinesApi::class)
    suspend fun logDaemon() {
        GlobalScope.launch {
            while (true) {
                processLog()
                delay(10000)
            }
        }
    }

    private suspend fun processLog() {
        logMessages.forEach {

            val guild = discord.kord.getGuild(it.key)
            if (guild == null) {
                logMessages.remove(it.key)
                return@forEach
            }

            val pingLogs: MutableList<LogMessage> = mutableListOf()
            val noPingLogs: MutableList<LogMessage> = mutableListOf()

            it.value.forEach { logMsg ->
                if (logMsg.shouldPing && pingLogs.size < 10) {
                    pingLogs.add(logMsg)
                }

                if (!logMsg.shouldPing && noPingLogs.size < 10) {
                    noPingLogs.add(logMsg)
                }
            }

            val logChannel = getLogConfig(it.key)

            log(guild, true, logChannel, pingLogs.joinToString("\n") { msg -> msg.logMsg })
            log(guild, false, logChannel, noPingLogs.joinToString("\n") { msg -> msg.logMsg })

            it.value.removeAll(pingLogs)
            it.value.removeAll(noPingLogs)

        }
    }

    private fun getLogConfig(guildSnowflake: Snowflake) = config[guildSnowflake]!!.logChannel

    private suspend fun log(guild: Guild, shouldPing: Boolean, logChannelId: Snowflake, message: String) {
        if (message.isNotEmpty()) {
            guild.getChannelOf<TextChannel>(logChannelId).createMessage {
                content = message

                if (!shouldPing) {
                    allowedMentions { }
                }
            }
        }
    }
}

