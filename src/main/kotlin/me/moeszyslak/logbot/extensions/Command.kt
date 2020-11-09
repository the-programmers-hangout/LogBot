package me.moeszyslak.logbot.extensions

import me.jakejmattson.discordkt.api.dsl.Command
import me.moeszyslak.logbot.services.DEFAULT_REQUIRED_PERMISSION
import me.moeszyslak.logbot.services.PermissionLevel
import java.util.*


private object CommandPropertyStore {
    val permissions = WeakHashMap<Command, PermissionLevel>()
}

var Command.requiredPermissionLevel: PermissionLevel
    get() = CommandPropertyStore.permissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        CommandPropertyStore.permissions[this] = value
    }