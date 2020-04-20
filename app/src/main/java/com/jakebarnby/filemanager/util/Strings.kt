package com.jakebarnby.filemanager.util

import java.util.*

fun String.stripExtension(): String {
    return if (lastIndexOf('.') > 0) {
        substring(0, lastIndexOf('.'))
    } else {
        this
    }
}

fun Long.toDisplayDate(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@toDisplayDate
    }

    return String.format(
        Locale.getDefault(),
        Constants.DATE_TIME_FORMAT,
        calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE],
        calendar[Calendar.DAY_OF_MONTH],
        calendar[Calendar.MONTH] + 1,
        calendar[Calendar.YEAR] - 2000
    )
}