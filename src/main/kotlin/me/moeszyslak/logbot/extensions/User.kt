package me.moeszyslak.logbot.extensions

import dev.kord.core.entity.User


fun User.descriptor() = "$username#$discriminator :: $mention"
fun User.simpleDescriptor() = "$mention ($username#$discriminator)"
fun User.idDescriptor() = "$username#$discriminator :: ${id.value}"
