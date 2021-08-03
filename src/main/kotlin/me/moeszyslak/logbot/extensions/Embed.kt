package me.moeszyslak.logbot.extensions

import dev.kord.rest.builder.message.EmbedBuilder


fun EmbedBuilder.createContinuableField(primaryTitle: String, content: String) = content
        .chunked(1024)
        .mapIndexed { index, chunk ->
            field {
                name = if (index == 0) primaryTitle else "(cont)"
                value = chunk
                inline = false
            }
        }