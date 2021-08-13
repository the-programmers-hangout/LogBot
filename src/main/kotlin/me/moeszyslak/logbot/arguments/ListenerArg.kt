package me.moeszyslak.logbot.arguments

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.moeszyslak.logbot.dataclasses.Listener

open class ListenerArg(override val name: String = "Listener", override val description: String = "A listener from the list: `${Listener.values().joinToString()}`"):
    Argument<Listener> {

    companion object: ListenerArg()

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = Listener.values().toList().map { it.value }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Listener> {
        val listener = Listener.values().toList().firstOrNull { it.value == arg.lowercase() }

        return if (listener != null) {
            Success(listener)
        } else {
            Error("That listener doesn't exist.")
        }
    }


}