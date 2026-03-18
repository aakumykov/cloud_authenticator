package com.github.aakumykov.cloud_authenticator.extensions

val Throwable.errorMsg: String get() = message ?: javaClass.name

val Throwable.errorMsgExtended: String get() = message?.let { "$it (${javaClass.name})" } ?: javaClass.name