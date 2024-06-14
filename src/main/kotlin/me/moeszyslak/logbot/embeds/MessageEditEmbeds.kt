package me.moeszyslak.logbot.embeds

import dev.kord.common.kColor
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.util.*
import me.moeszyslak.logbot.extensions.createContinuableField
import me.moeszyslak.logbot.services.CachedMessage
import java.awt.Color
import java.time.Instant

fun formatTimeStamp(timestamp: Instant) =
    TimeStamp.at(timestamp, TimeStyle.RELATIVE) + "\n" + TimeStamp.at(timestamp, TimeStyle.DATETIME_LONG)

fun User.formatDisplay() = "$mention ($fullName)"

fun EmbedBuilder.createMessageDeleteEmbed(cachedMessage: CachedMessage) {
    title = "Message Deleted"
    color = Color.RED.kColor

    addInlineField("User", cachedMessage.user.formatDisplay())
    addInlineField("Channel", cachedMessage.channel.mention)

    createContinuableField("Content", cachedMessage.content)

    if (cachedMessage.attachments.isNotEmpty()) {
        field {
            name = "Attachments"
            value = "```${cachedMessage.attachments.joinToString("\n") { it.filename }}```"
        }
    }

    addInlineField("Sent at", formatTimeStamp(cachedMessage.timestamp))
}

fun EmbedBuilder.createMessageEditedEmbed(newMessage: CachedMessage, cachedMessage: CachedMessage) {
    title = "Message Edited"
    color = Color.ORANGE.kColor

    addInlineField("User", cachedMessage.user.formatDisplay())
    addInlineField("Channel", cachedMessage.channel.mention)

    field {
        name = "Link"
        value =
            "[Jump To](https://discord.com/channels/${cachedMessage.guildId}/${cachedMessage.channel.id}/${cachedMessage.messageId})"
        inline = true
    }

    createContinuableField("Old", cachedMessage.content)
    createContinuableField("New", newMessage.content)

    addInlineField("Original", formatTimeStamp(cachedMessage.timestamp))
    addInlineField("Edited", formatTimeStamp(newMessage.timestamp))
}