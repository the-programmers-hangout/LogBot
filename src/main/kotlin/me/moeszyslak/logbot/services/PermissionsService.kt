package me.moeszyslak.logbot.services

import com.gitlab.kordlib.core.entity.Member
import kotlinx.coroutines.flow.toList
import me.moeszyslak.logbot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.annotations.Service

enum class PermissionLevel {
    BotOwner,
    GuildOwner,
    Administrator,
    Staff,
    Everyone
}

val DEFAULT_REQUIRED_PERMISSION = PermissionLevel.Staff

@Service
class PermissionsService(private val configuration: Configuration) {
    suspend fun hasClearance(member: Member, requiredPermissionLevel: PermissionLevel) = member.getPermissionLevel().ordinal <= requiredPermissionLevel.ordinal

    private suspend fun Member.getPermissionLevel() =
            when {
                isBotOwner() -> PermissionLevel.BotOwner
                isGuildOwner() -> PermissionLevel.GuildOwner
                isAdministrator() -> PermissionLevel.Administrator
                isStaff() -> PermissionLevel.Staff
                else -> PermissionLevel.Everyone
            }

    private fun Member.isBotOwner() = id.longValue == configuration.botOwner
    private suspend fun Member.isGuildOwner() = isOwner()
    private suspend fun Member.isAdministrator(): Boolean {
        val config = configuration[guild.id.longValue] ?: return false

        return roles.toList().any { it.id.longValue == config.adminRole }
    }

    private suspend fun Member.isStaff(): Boolean {
        val config = configuration[guild.id.longValue] ?: return false

        return roles.toList().any { it.id.longValue == config.staffRole }
    }
}