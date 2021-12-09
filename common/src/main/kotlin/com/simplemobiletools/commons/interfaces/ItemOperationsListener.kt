package com.simplemobiletools.commons.interfaces

import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.FolderItem
import java.util.*

interface ItemOperationsListener {
    fun refreshItems(isHeaderFolder : Boolean)

    fun deleteFiles(files: ArrayList<FileDirItem>)

    fun selectedPaths(paths: ArrayList<String>)

    fun headerFolderClick(folder : FolderItem)

}
