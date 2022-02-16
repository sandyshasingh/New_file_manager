package com.rocks.chromecast.exoplayer

import com.editor.hiderx.database.HiddenFiles

interface ExoPlayerListener {

    fun playPrevious()
    fun playNext()
    fun playCurrent()
    fun playVideoAtIndex(videoFile: HiddenFiles?, position: Int)
    fun playCurrent(seekTime: Long)
}
