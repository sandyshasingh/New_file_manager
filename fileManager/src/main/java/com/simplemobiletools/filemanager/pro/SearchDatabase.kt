package com.simplemobiletools.filemanager.pro

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DatabaseforSearch")
data class SearchDatabase(
    @PrimaryKey
    var mPath: String,

    @ColumnInfo(name = "mName")
    val mName:String?="",

    @ColumnInfo(name = "mIsDirectory")
    var mIsDirectory:Boolean = false,

    @ColumnInfo(name = "mChildren")
    var mChildren: Int = 0,

    @ColumnInfo(name = "mSize")
    var mSize: Long = 0L,

    @ColumnInfo(name = "mModified")
    var mModified: Long = 0L,

    @ColumnInfo(name = "isSectionTitle")
    var isSectionTitle: Boolean,

    @ColumnInfo(name = "audioImageUri")
    var audioImageUri: String?,

    @ColumnInfo(name = "dateModifiedInFormat")
    var dateModifiedInFormat: String,

    @ColumnInfo(name = "mimeType")
    var mimeType: String?
)

