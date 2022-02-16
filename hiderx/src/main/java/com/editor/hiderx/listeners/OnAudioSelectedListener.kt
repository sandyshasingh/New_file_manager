package com.editor.hiderx.listeners

import com.editor.hiderx.database.HiddenFiles

interface OnAudioSelectedListener {

    fun onAudioDeselected(audio : HiddenFiles)
    fun onAudioSelected(audio : HiddenFiles)
    fun onAudioClicked(audio: List<HiddenFiles>, adapterPosition: Int)
    fun onAudioFolderClicked(audio : HiddenFiles)

}