package com.simplemobiletools.filemanager.pro.helpers

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Environment
import com.simplemobiletools.commons.ListItem
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class DocumentFetcher(var context: Context, var fetchAudioAsyncCompleteListener: FetchDocumentsAsyncCompleteListener,) : AsyncTask<Void, Void, ArrayList<ListItem>>() {

    var fileDataClassList: ArrayList<ListItem> = ArrayList()
    //  var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()


    override fun doInBackground(vararg p0: Void?): ArrayList<ListItem>? {

        return fetchApk()
    }


    fun searchDir(dir: File) {
        val docPattern1 = ".pdf"
        val docPattern2 = ".docx"
        val fileList = dir.listFiles()
        val sharedPrefFile = "com.example.new_file_manager"
        var last_login_time :Long =0L
        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        last_login_time = sharedPreferences?.getLong("LAST_LOGIN",0L)!!
        if (fileList != null) {
            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    searchDir(fileList[i])
                }
                else if(fileList[i].path.endsWith(docPattern1)) {
                    var isNew = false
                    if (fileList[i].lastModified()> last_login_time)
                        isNew = true
                    //val size = format(fileList[i].length().toDouble(),2)
                    val hoho = ListItem(
                        fileList[i].path,
                        fileList[i].name,
                        false,
                        0,
                        fileList[i].length(),
                        0L,
                        false,
                        null,
                        "",
                        "",
                        isNew
                    )
                    fileDataClassList.add(hoho)
                }
                else if(fileList[i].path.endsWith(docPattern2)) {
                    var isNew = false
                    if (fileList[i].lastModified()> last_login_time)
                        isNew = true
                    //val size = format(fileList[i].length().toDouble(),2)
                    val hoho = ListItem(
                        fileList[i].path,
                        fileList[i].name,
                        false,
                        0,
                        fileList[i].length(),
                        0L,
                        false,
                        null,
                        "",
                        "",
                        isNew
                    )
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

        fetchAudioAsyncCompleteListener.fetchDocumentsCompleted(result)
    }
    interface FetchDocumentsAsyncCompleteListener {
        fun fetchDocumentsCompleted(documentsList:ArrayList<ListItem>? )
    }

}
