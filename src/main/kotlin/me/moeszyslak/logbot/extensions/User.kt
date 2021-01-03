package me.moeszyslak.logbot.extensions

import com.gitlab.kordlib.core.entity.User

fun User.descriptor() = "$username#$discriminator :: $mention"
fun User.simpleDescriptor() = "$mention ($username#$discriminator)"
fun User.idDescriptor() = "$username#$discriminator :: ${id.longValue}"
