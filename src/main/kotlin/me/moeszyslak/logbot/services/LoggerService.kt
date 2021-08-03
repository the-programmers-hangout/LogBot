package me.moeszyslak.logbot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.extensions.descriptor
import me.moeszyslak.logbot.extensions.idDescriptor
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class LoggerService(private val config: Configuration) {
    private fun withLog(guild: Guild, shouldPing: Boolean = true, f: () -> String) =
            getLogConfig(guild).apply {
                runBlocking {
                    log(guild, shouldPing, getLogConfig(guild), f())
                }
            }


    /*

        MemberJoinEvent

     */
    fun memberJoin(guild: Guild, member: Member) = withLog(guild) {
        "${member.descriptor()} created at ${LocalDateTime.ofInstant(member.id.timeStamp.toJavaInstant(), ZoneOffset.UTC)} joined the server"
    }

    /*

        MemberLeaveEvent

     */

    fun memberLeave(guild: Guild, user: User) = withLog(guild) {
        "${user.descriptor()} created at ${LocalDateTime.ofInstant(user.id.timeStamp.toJavaInstant(), ZoneOffset.UTC)} left the server"
    }

    fun voiceChannelJoin(guild: Guild, user: User, channelId: Snowflake) = withLog(guild, false) {
        "${user.idDescriptor()} joined voice channel <#${channelId.value}>"
    }

    fun voiceChannelLeave(guild: Guild, user: User, channelId: Snowflake) = withLog(guild, false) {
        "${user.idDescriptor()} left voice channel <#${channelId.value}>"
    }

    private fun getLogConfig(guild: Guild) = config[guild.id.value]!!.logChannel.toSnowflake()

    private suspend fun log(guild: Guild, shouldPing: Boolean, logChannelId: Snowflake, message: String) =
        guild.getChannelOf<TextChannel>(logChannelId).createMessage {
            content = message

            if (!shouldPing) {
                allowedMentions { }
            }
        }

}

