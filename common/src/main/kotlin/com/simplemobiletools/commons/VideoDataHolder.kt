package com.simplemobiletools.commons


enum class VideoDataHolder {
    INSTANCE;

    var mObjectList: ArrayList<ListItem>? = null
    private var mFilesList: ArrayList<ListItem>? = null


    companion object {
        fun hasData(): Boolean {
            return INSTANCE.mObjectList != null
        }

        var data: ArrayList<ListItem>?
            get() = INSTANCE.mObjectList
            set(objectList) {
                INSTANCE.mObjectList = objectList
            }

        fun getFileExt(fileName: String): String {
            return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
        }

        var filesData :ArrayList<ListItem>?
        get() = INSTANCE.mFilesList
        set(mFilesList){
            INSTANCE.mFilesList = mFilesList
        }

    }
}