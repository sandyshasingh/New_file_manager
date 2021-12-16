package com.simplemobiletools.filemanager.pro.helpers

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.filemanager.pro.models.ListItem
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class AppsFetcher(var context: Context, var fetchAudioAsyncCompleteListener: FetchAppsAsyncCompleteListener,) : AsyncTask<Void, Void, ArrayList<ListItem>>() {

    var fileDataClassList: ArrayList<ListItem> = ArrayList()
  //  var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()


    override fun doInBackground(vararg p0: Void?): ArrayList<ListItem>? {

        return fetchApk()
    }


    fun searchDir(dir: File) {
        val pdfPattern = ".apk"
        val fileList = dir.listFiles()
        if (fileList != null) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    searchDir(fileList[i])
                }
                else if(fileList[i].path.endsWith(pdfPattern)) {
                    //val size = format(fileList[i].length().toDouble(),2)
                    val hoho = ListItem(fileList[i].path,fileList[i].name,false,0,fileList[i].length(),0L,false,null)
                    fileDataClassList.add(hoho)
                }
            }
        }
    }


    fun fetchApk(): ArrayList<ListItem> {
        val parentFile = Environment.getExternalStorageDirectory()
        var fileList : Array<File>? = null
        if(parentFile?.exists()!!)
        {
            searchDir(parentFile)
        }
        Collections.sort(fileDataClassList,
            Comparator<ListItem> { o1 : ListItem, o2 : ListItem ->
                when {
                    File(o2.path).lastModified()>File(o1.path).lastModified() -> return@Comparator 1
                    File(o2.path).lastModified()<File(o1.path).lastModified() -> return@Comparator -1
                    else -> return@Comparator 0
                }
            })
        return fileDataClassList
    }

    override fun onPostExecute(result: ArrayList<ListItem>?) {
        super.onPostExecute(result)

        fetchAudioAsyncCompleteListener.fetchAppsCompleted(result)
    }
    interface FetchAppsAsyncCompleteListener {
        fun fetchAppsCompleted(audiosList:ArrayList<ListItem>? )
    }

}
