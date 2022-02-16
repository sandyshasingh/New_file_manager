package com.editor.hiderx

import android.text.format.DateFormat
import java.util.*

object Utility {
    const val KEY_STORAGE_PERMISSION: String = "STORAGE_PERMISSION"
    const val KEY_CHANGE_PASSWORD : String = "CHANGE_PASSWORD"
    const val KEY_FROM_HOME_SCREEN : String = "FROM_HOME_SCREEN"
    const val IS_CALCULATOR = "SET_PASS_WORD_EXTRA"
    const val FLASH_OFF = 0
    const val FLASH_ON = 1
    const val FLASH_AUTO = 2
    const val STATE_INIT = 0
    const val STATE_PREVIEW = 1
    const val STATE_PICTURE_TAKEN = 2
    const val STATE_WAITING_LOCK = 3
    const val STATE_WAITING_PRECAPTURE = 4
    const val STATE_WAITING_NON_PRECAPTURE = 5
    const val STATE_STARTING_RECORDING = 6
    const val STATE_STOPING_RECORDING = 7
    const val STATE_RECORDING = 8

    fun convertIntoDate(smsTime: Calendar): String? {
        val now = Calendar.getInstance()
        return if (now[Calendar.DATE] == smsTime[Calendar.DATE] && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            "Today " /*+ DateFormat.format(timeFormatString, smsTime);*/
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1 && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            "Yesterday" /*+ DateFormat.format(timeFormatString, smsTime);*/
        } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] in 2..4 && now[Calendar.MONTH] == smsTime[Calendar.MONTH]) {
            DateFormat.format("EEEE", smsTime).toString()
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
            DateFormat.format("EEEE, dd MMM", smsTime).toString()
        } else {
            DateFormat.format("EEEE, dd MMM yyyy", smsTime).toString()
        }
    }

}