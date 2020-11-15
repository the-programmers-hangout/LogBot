package me.moeszyslak.logbot.embeds

import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.moeszyslak.logbot.extensions.createContinuableField
import me.moeszyslak.logbot.extensions.simpleDescriptor
import me.moeszyslak.logbot.services.CachedMessage
import java.awt.Color
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun EmbedBuilder.createMessageDeleteEmbed(cachedMessage: CachedMessage) {
    title = "Message Deleted"
    color = Color.RED

    field {
        name = "User"
        value = cachedMessage.user.simpleDescriptor()
        inline = true
    }

    field {
        name = "Channel"
        value = cachedMessage.channel.mention
        inline = true
    }

    createContinuableField("Content", cachedMessage.content)

    if (cachedMessage.attachments.isNotEmpty()) {
        field {
            name = "Attachments"
            value = "```${cachedMessage.attachments.joinToString("\n") { it.filename }}```"
        }
    }

    field {
        name = "Sent at"
        value = cachedMessage.timestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        inline = true
    }

}

fun EmbedBuilder.createMessageEditedEmbed(newMessage: CachedMessage, cachedMessage: CachedMessage) {
    title = "Message Deleted"
    color = Color.ORANGE

    field {
        name = "User"
        value = cachedMessage.user.simpleDescriptor()
        inline = true
    }

    field {
        name = "Channel"
        value = cachedMessage.channel.mention
        inline = true
    }

    createContinuableField("Old", cachedMessage.content)
    createContinuableField("New", newMessage.content)


    field {
        name = "Old message sent at"
        value = cachedMessage.timestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        inline = true
    }

    field {
        name = "New message sent at"
        value = newMessage.timestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        inline = true
    }
}