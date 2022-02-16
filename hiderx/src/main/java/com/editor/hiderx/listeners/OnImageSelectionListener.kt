package com.editor.hiderx.listeners

import com.editor.hiderx.database.HiddenFiles

interface OnImageSelectionListener {
    fun onImageDeselected(hiddenPhotos: HiddenFiles)
    fun onImageSelected(hiddenPhotos: HiddenFiles)
    fun onImageClicked(hiddenPhotos: List<HiddenFiles>, position: Int)
    fun onImageFolderClicked(hiddenPhotos: HiddenFiles)
}
