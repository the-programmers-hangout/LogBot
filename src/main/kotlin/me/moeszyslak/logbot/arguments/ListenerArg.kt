package me.moeszyslak.logbot.arguments

import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.moeszyslak.logbot.dataclasses.Listener

open class ListenerArg(override val name: String = "Listener"): ArgumentType<Listener>() {

    companion object: ListenerArg()

    override fun generateExamples(event: CommandEvent<*>): List<String> = Listener.values().toList().map { it.value }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Listener> {
        val listener = Listener.values().toList().firstOrNull { it.value == arg.toLowerCase() }

        return if (listener != null) {
            Success(listener)
        } else {
            Error("That listener doesn't exist.")
        }
    }


}