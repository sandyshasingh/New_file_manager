package com.editor.hiderx.listeners

import com.editor.hiderx.database.HiddenFiles

interface OnVideoSelectedListener {
    fun onVideoSelected(hiddenVideos: HiddenFiles)
    fun onVideoDeselected(hiddenVideos: HiddenFiles)
    fun onVideoClicked(hiddenVideos: List<HiddenFiles>,position:Int)
    fun onVideoFolderClicked(hiddenVideos: HiddenFiles)
}
