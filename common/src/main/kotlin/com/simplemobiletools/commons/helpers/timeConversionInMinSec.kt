package com.simplemobiletools.commons.helpers

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.concurrent.TimeUnit

fun timeConversionInMinSec(milisec: Int): String? {
    val millis = milisec.toLong()
    return if(milisec>=3600000){
        timeConversionInHHMMSS(milisec.toLong())
    }else {
        String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
    }
}
fun timeConversionInHHMMSS(millis: Long) : String{
    return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    )
}
fun format(bytes: Double, digits: Int): String {
    var byte = bytes
    val dictionary = arrayOf("bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    var index = 0
    while (index < dictionary.size) {
        if (byte < 1024) {
            break
        }
        byte /= 1024
        index++
    }
    return String.format("%." + digits + "f", byte) + " " + dictionary[index]
}

fun getFolderSize(dir : File, context : Context): Long {
    var size: Long = 0
    val files = dir.listFiles()
    if(files!=null && files.isNotEmpty()) {
        for (file in files) {
            size += if (file.isFile) {
                file.length()
            } else getFolderSize(file, context)
        }
    }
    return size
}