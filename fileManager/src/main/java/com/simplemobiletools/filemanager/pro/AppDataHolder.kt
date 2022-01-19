package com.simplemobiletools.filemanager.pro

enum class AppDataHolder {




    INSTANCE;



     var mfinalValues:RecentUpdatedFiles? = null


    companion object {



        fun hasFinalData(): Boolean {
            return INSTANCE.mfinalValues != null
        }




        var mfinalValues: RecentUpdatedFiles?
            get() = INSTANCE.mfinalValues
            set(objectList) { INSTANCE.mfinalValues = objectList
            }
    }



}
