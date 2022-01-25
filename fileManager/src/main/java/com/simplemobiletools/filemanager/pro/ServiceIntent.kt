package com.simplemobiletools.filemanager.pro

import android.app.IntentService
import android.content.Intent
import com.simplemobiletools.commons.extensions.getDirectChildrenCount
import com.simplemobiletools.commons.extensions.getMimeType
import com.simplemobiletools.commons.extensions.getProperSize
import com.simplemobiletools.commons.helpers.SORT_BY_SIZE
import com.simplemobiletools.filemanager.pro.extensions.config
import com.simplemobiletools.filemanager.pro.extensions.getAudioImageFromPath
import java.io.File
import java.util.HashMap

const val TAG = "search_tag"
class ServiceIntent:IntentService(TAG) {

   // var list = ArrayList()
    override fun onHandleIntent(p0: Intent?) {
        val internalStoragePath = this?.config?.internalStoragePath
       DatabaseforSearch.getInstance(applicationContext)?.searchDatabaseDao()?.deleteAll()
        search("$internalStoragePath")
    }



    fun search(path:String){
        var file = File(path)
        val list = file.listFiles()
        val sorting = this.config.getFolderSorting(path)
        val isSortingBySize = sorting and SORT_BY_SIZE != 0

        for(i in list)
        {
            if (i.isDirectory) {
                val fileDirItem = getFileDirItemFromFile(i, isSortingBySize, HashMap())
                if(fileDirItem != null)
                {
                    DatabaseforSearch.getInstance(applicationContext)?.searchDatabaseDao()?.insertSearchResult(fileDirItem)
                    search(i.path)
                }
            }
            else {
                val fileDirItem = getFileDirItemFromFile(i, isSortingBySize, HashMap())
                if(fileDirItem != null)
                {
                    DatabaseforSearch.getInstance(applicationContext)?.searchDatabaseDao()?.insertSearchResult(fileDirItem)
                }
            }
        }

    }
    private fun getFileDirItemFromFile(file: File, isSortingBySize: Boolean, lastModifieds: HashMap<String, Long>): SearchDatabase? {
        val curPath = file.absolutePath
        val curName = file.name
//        if (!showHidden && curName.startsWith(".") ) {
//            return null
//        }
        if(file.length()<=0 ){
            return null
        }

        var lastModified = lastModifieds.remove(curPath)
        val isDirectory = if (lastModified != null) false else file.isDirectory
        val children = if (isDirectory) file.getDirectChildrenCount(false) else 0
        val size = if (isDirectory) {
            if (isSortingBySize) {
                file.getProperSize(false)
            } else {
                0L
            }
        } else {
            file.length()
        }

        if (lastModified == null) {
            lastModified = file.lastModified()
        }
       // val newUri = applicationContext.getFinalUriFromPath(file.path, "com.rocks.music.videoplayer.provider")

        val audioImageUri = if (file.path.getMimeType().contains("audio")) {
            this?.let { getAudioImageFromPath(it, file.path) }
        } else null

        return SearchDatabase(
            curPath,
            curName,
            isDirectory,
            children,
            size,
            lastModified,
            false,
            audioImageUri.toString(),
            "",
            ""
        )
    }
}