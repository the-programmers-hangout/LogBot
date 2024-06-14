package me.moeszyslak.logbot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.allowedMentions
import kotlinx.coroutines.*
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.util.TimeStamp
import me.jakejmattson.discordkt.util.TimeStyle
import me.jakejmattson.discordkt.util.descriptor
import me.moeszyslak.logbot.dataclasses.Configuration

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

    fun serverJoin(guild: Guild, member: Member) =
        addToLog(guild, "USER JOINED :: ${member.descriptor()} " +
            "(created ${TimeStamp.at(member.id.timestamp.toJavaInstant(), TimeStyle.RELATIVE)})")

    fun serverLeave(guild: Guild, user: User) =
        addToLog(guild, "USER LEFT :: ${user.descriptor()}")

    fun voiceJoin(guild: Guild, user: User, channelId: Snowflake) =
        addToLog(guild, "VOICE JOIN :: <#${channelId.value}> :: ${user.descriptor()}", false)

    fun voiceLeave(guild: Guild, user: User, channelId: Snowflake) =
        addToLog(guild, "VOICE LEAVE :: <#${channelId.value}> :: ${user.descriptor()}", false)

    fun reactionAdd(guild: Guild, reaction: ReactionEmoji, member: Member, channel: MessageChannelBehavior, jumpUrl: String) =
        addToLog(guild, "REACTION ADD :: ${member.descriptor()} :: ${reaction.mention} :: ${channel.mention} :: $jumpUrl", false)

    fun reactionRemove(guild: Guild, reaction: ReactionEmoji, member: Member, channel: MessageChannelBehavior, jumpUrl: String) =
        addToLog(guild, "REACTION REMOVE :: ${member.descriptor()} :: ${reaction.mention} :: ${channel.mention} :: $jumpUrl", false)

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

            val guild = discord.kord.getGuildOrNull(it.key)
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

