package com.editor.hiderx.listeners

import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.dataclass.FileDataClass

interface OnPagerItemsClickLister {
    fun onItemRemoved(hiddenFiles: HiddenFiles)
    fun onFileRemoved(fileDataClass: FileDataClass)
}
