package com.simplemobiletools.filemanager.pro

import com.simplemobiletools.filemanager.pro.models.ListItem

enum class AppDataHolder {




    INSTANCE;



    lateinit var mfinalValues:RecentUpdatedFiles


    companion object {



        fun hasFinalData(): Boolean {
            return INSTANCE.mfinalValues != null
        }




        var mfinalValues: RecentUpdatedFiles
            get() = INSTANCE.mfinalValues
            set(objectList) { INSTANCE.mfinalValues = objectList
            }
    }



}
