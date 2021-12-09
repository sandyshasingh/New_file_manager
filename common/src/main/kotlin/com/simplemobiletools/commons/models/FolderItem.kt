package com.simplemobiletools.commons.models

import android.graphics.drawable.Drawable

data class FolderItem(var id: Int, var folderName: String, var folderIcon: Int, var backgroundColor: Drawable, var textColor: Int, var ClickCount: Long, var size: Long = 0L,var sizeString: String? = "") {
}