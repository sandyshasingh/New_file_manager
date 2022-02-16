package com.editor.hiderx

import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.dataclass.FileDataClass

enum class VideoDataHolder {
    INSTANCE;

    private var mObjectList: List<HiddenFiles>? = null
    private var mFilesList: List<FileDataClass>? = null


    companion object {
        fun hasData(): Boolean {
            return INSTANCE.mObjectList != null
        }

        var data: List<HiddenFiles>?
            get() = INSTANCE.mObjectList
            set(objectList) {
                INSTANCE.mObjectList = objectList
            }

        fun getFileExt(fileName: String): String {
            return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
        }

        var filesData : List<FileDataClass>?
        get() = INSTANCE.mFilesList
        set(mFilesList){
            INSTANCE.mFilesList = mFilesList
        }

    }
}