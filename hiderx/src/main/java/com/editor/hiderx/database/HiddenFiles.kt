package com.editor.hiderx.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class HiddenFiles  @JvmOverloads constructor(
    @NonNull
    @PrimaryKey
    var path: String,

    @ColumnInfo(name = "name")
    var name: String?,

    @ColumnInfo(name = "originalPath")
    var originalPath: String?,

    @ColumnInfo(name = "size")
    var size: String?,

    @ColumnInfo(name = "type")
    var type: String?,

    @ColumnInfo(name = "updateTime")
    var updateTime: Long?,

    @Ignore var isSelected: Boolean = false,
    @Ignore var isFile: Boolean? = false,
    @Ignore var count: Int? = 0
) : Serializable