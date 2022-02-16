package com.editor.hiderx.dataclass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FileDataClass(var path: String, val name: String?, var size: String?, var isFile: Boolean, var noOfItems: Int?, var mimeType: String?, var isSelected: Boolean, var updateTimeStamp: Long) : Parcelable