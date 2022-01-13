package com.simplemobiletools.filemanager.pro

import com.simplemobiletools.filemanager.pro.models.ListItem

enum class AppDataHolder {



    INSTANCE;

    private var mFinalList: Map<String,List<ListItem>>? = null


    companion object {



        fun hasFinalData(): Boolean {
            return INSTANCE.mFinalList != null
        }


        var finalDataList:  Map<String,List<ListItem>>?
            get() = INSTANCE.mFinalList
            set(objectList) { INSTANCE.mFinalList = objectList
            }
    }



}

