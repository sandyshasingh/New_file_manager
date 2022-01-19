package com.simplemobiletools.commons

enum class DataHolderforImageViewer {

    INSTANCE;



    var mfinalValues:ArrayList<ListItem>? = null


    companion object {



        fun hasFinalData(): Boolean {
            return INSTANCE.mfinalValues != null
        }




        var mfinalValues: ArrayList<ListItem>?
            get() = INSTANCE.mfinalValues
            set(objectList) { INSTANCE.mfinalValues = objectList
            }
    }




}