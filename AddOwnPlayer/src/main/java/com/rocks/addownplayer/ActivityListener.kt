package com.rocks.addownplayer

import android.net.Uri

interface ActivityListener {
    fun onErrorInData()
    fun onPaused()
    fun onPlay()
    fun unboundedService()
}
