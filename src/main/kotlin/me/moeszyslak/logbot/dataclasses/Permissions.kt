package me.moeszyslak.logbot.dataclasses

import dev.kord.common.entity.Permission
import dev.kord.core.any
import me.jakejmattson.discordkt.api.dsl.PermissionContext
import me.jakejmattson.discordkt.api.dsl.PermissionSet
import me.jakejmattson.discordkt.api.extensions.toSnowflake

enum class Permissions : PermissionSet {
    BOT_OWNER {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            return context.discord.getInjectionObjects<Configuration>().botOwner == context.user.id.value
        }
    },
    GUILD_OWNER {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            return member.isOwner()
        }
    },
    STAFF {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            val configuration = context.discord.getInjectionObjects<Configuration>()
            return member.roles.any { it.id.value == configuration[guild.id.value]?.staffRole}
        }
    },
    ADMINISTRATOR {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            val configuration = context.discord.getInjectionObjects<Configuration>()
            return member.roles.any { it.id.value == configuration[guild.id.value]?.adminRole} || member.getPermissions()
                .contains(
                    Permission.Administrator
                )
        }
    },
    NONE {
        override suspend fun hasPermission(context: PermissionContext) = true
    }
}