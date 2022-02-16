package com.rocks.addownplayer

import android.graphics.Bitmap
import android.net.Uri
import java.util.ArrayList


object PlayerUtils {
    var artwork : Bitmap? = null
    var artist : String? = null
    var pathList: ArrayList<String>? = null
    var positionInList : Int? = null
    var appName: String? = null
    const val NOTIFICATION_ID: Int = 101
    const val ACTION_PLAY_PREV: String = "ACTION_PLAY_PREVIOUS"
    const val INITIATE_HANDLER: String = "INITIATE_HANDLER"
    const val ARTIST_EXTRA: String = "ARTIST_EXTRA"
    const val THUMBNAIL_EXTRA: String = "THUMBNAIL_EXTRA"
    const val ACTION_PLAY_NEXT: String = "ACTION_PLAY_NEXT"
    const val TOGGLE_PAUSE_PLAY: String = "TOGGLE_PAUSE_PLAY"
    const val STOP_SERVICE: String = "STOP_SERVICE"
    const val LIST_EXTRA = "PATH_LIST"
    const val ACTION_SET_PLAYER = "ACTION_SET_PLAYER"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val RADIO_FM_APP = "RADIO_FM_APP"
    const val DISPLAY_CURRENT_DATA  = "DISPLAY_CURRENT_DATA"
    const val POSITION_EXTRA = "POSITION_IN_LIST"
    const val APP_NAME = "APP_NAME"
    const val CURRENT_TIME = "CURRENT_TIME"
    const val DURATION = "DURATION"
    const val DETAILS = "DETAILS"
    var mService : RocksPlayerService? = null

    fun decode(enc: String, offset: Int): String? {
        return encode(enc, 26 - offset)
    }

    fun encode(enc: String, offset: Int): String? {
        var offset = offset
        offset = offset % 26 + 26
        val encoded = StringBuilder()
        for (i in enc.toCharArray()) {
            if (Character.isLetter(i)) {
                if (Character.isUpperCase(i)) {
                    encoded.append(('A'.toInt() + (i - 'A' + offset) % 26).toChar())
                } else {
                    encoded.append(('a'.toInt() + (i - 'a' + offset) % 26).toChar())
                }
            } else {
                encoded.append(i)
            }
        }
        return encoded.toString()
    }

}