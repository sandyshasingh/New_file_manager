package com.simplemobiletools.commons

import android.net.Uri
import com.simplemobiletools.commons.models.FileDirItem

data class ListItem(
    val mPath: String,
    val mName: String = "",
    val mIsDirectory: Boolean = false,
    val mChildren: Int = 0,
    val mSize: Long = 0L,
    val mModified: Long = 0L,
    val isSectionTitle: Boolean,
    val audioImageUri: Uri?,
    var dateModifiedInFormat: String,
    var mimeType: String?,
    var isNewlyAdded:Boolean=false
) : FileDirItem(mPath, mName, mIsDirectory, mChildren, mSize, mModified)