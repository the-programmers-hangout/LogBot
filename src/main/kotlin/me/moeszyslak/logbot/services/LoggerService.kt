package me.moeszyslak.logbot.services

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.TextChannel
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import me.moeszyslak.logbot.dataclasses.Configuration
import me.moeszyslak.logbot.extensions.descriptor
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class LoggerService(private val config: Configuration) {
    private fun withLog(guild: Guild, f: () -> String) =
            getLogConfig(guild).apply {
                runBlocking {
                    log(guild, getLogConfig(guild), f())
                }
            }

    /*

        MemberJoinEvent

     */
    fun memberJoin(guild: Guild, member: Member) = withLog(guild) {
        "${member.descriptor()} created at ${LocalDateTime.ofInstant(member.id.timeStamp, ZoneOffset.UTC)} joined the server"
    }

    /*

        MemberLeaveEvent

     */

    fun memberLeave(guild: Guild, user: User) = withLog(guild) {
        "${user.descriptor()} created at ${LocalDateTime.ofInstant(user.id.timeStamp, ZoneOffset.UTC)} left the server"
    }

    private fun getLogConfig(guild: Guild) = config[guild.id.longValue]!!.logChannel.toSnowflake()
    private suspend fun log(guild: Guild, logChannelId: Snowflake, message: String) = guild.getChannelOf<TextChannel>(logChannelId).createMessage(message)
}

