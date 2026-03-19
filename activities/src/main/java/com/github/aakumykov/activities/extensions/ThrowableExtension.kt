package com.github.aakumykov.activities.extensions

val Throwable.errorMsg: String get() = message ?: javaClass.name

val Throwable.errorMsgExtended: String get() = message?.let { "$it (${javaClass.name})" } ?: javaClass.name