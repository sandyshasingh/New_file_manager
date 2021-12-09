package com.simplemobiletools.commons.extensions

import android.graphics.Color
import java.util.*

fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("0:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start


fun Int.ensureTwoDigits(): String {
    return if (toString().length == 1) {
        "0$this"
    } else {
        toString()
    }
}

