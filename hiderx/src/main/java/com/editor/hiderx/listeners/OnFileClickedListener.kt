package com.editor.hiderx.listeners

import com.editor.hiderx.dataclass.FileDataClass

interface OnFileClickedListener {
    fun onFileDeselected(fileDataClass: FileDataClass)
    fun onFileSelected(fileDataClass: FileDataClass)
    fun onFolderClicked(fileDataClass: FileDataClass)
    fun onFileClicked(listOfFiles: ArrayList<FileDataClass>, position: Int)
}
