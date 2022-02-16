package com.editor.hiderx.listeners

import com.editor.hiderx.database.HiddenFiles

interface onUploadClickListenerForCamera {
    fun onUploadPhotoClicked(path : String)
    fun onUploadVideoClicked(path : String)
    fun onFileClicked(hiddenFiles: List<HiddenFiles>,position:Int)
}
