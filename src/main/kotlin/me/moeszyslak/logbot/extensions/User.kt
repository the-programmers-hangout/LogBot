package me.moeszyslak.logbot.extensions

import com.gitlab.kordlib.core.entity.User

fun User.descriptor() = "${this.username}#${this.discriminator} :: ${this.mention}"