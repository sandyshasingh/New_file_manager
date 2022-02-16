package com.editor.hiderx.listeners

interface FragmentInteractionListener {

    fun setDirectoryToFolder(folderName : String?,position: Int?)
    fun dismissDialog()
    fun getSelectedFolder(): String?
    fun setDirectoryToDefault()
    fun showNewFolderDialog()

}
