package com.editor.hiderx.listeners

import com.editor.hiderx.dataclass.SimpleDataClass


interface OnItemSelectedListener {
    fun onItemSelected(item : SimpleDataClass)
    fun onItemDeselected(item : SimpleDataClass)
    fun onItemClicked(listOfFiles: List<SimpleDataClass>,position : Int)
}
