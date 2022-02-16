package com.editor.hiderx.dataclass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SimpleDataClass(var path : String?, val name : String?,var isSelected : Boolean) : Parcelable