package me.moeszyslak.logbot.embeds

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.moeszyslak.logbot.extensions.simpleDescriptor
import java.awt.Color

fun EmbedBuilder.createVoiceJoinEmbed(user: User, channelId: Snowflake) {
    title = "Voice Channel Joined"
    color = Color.GREEN

    field {
        name = "User"
        value = user.simpleDescriptor()
        inline = true
    }

    field {
        name = "Channel"
        value = "<#${channelId.value}>"
        inline = true
    }
}

fun EmbedBuilder.createVoiceLeaveEmbed(user: User, channelId: Snowflake) {
    title = "Voice Channel Left"
    color = Color.RED

    field {
        name = "User"
        value = user.simpleDescriptor()
        inline = true
    }

    field {
        name = "Channel"
        value = "<#${channelId.value}>"
        inline = true
    }
}
