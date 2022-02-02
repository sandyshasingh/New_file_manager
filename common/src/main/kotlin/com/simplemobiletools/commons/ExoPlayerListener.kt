package com.simplemobiletools.commons


interface ExoPlayerListener {

    fun playPrevious()
    fun playNext()
    fun playCurrent()
    fun playVideoAtIndex(videoFile: ListItem?, position: Int)
    fun playCurrent(seekTime: Long)
}
