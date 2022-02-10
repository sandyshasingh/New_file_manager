package com.simplemobiletools.filemanager.pro.helpers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.filemanager.pro.GroupVideoPhotoAsyncTask
import com.simplemobiletools.filemanager.pro.RecentUpdatedFiles

class DataViewModel(application: Application):  AndroidViewModel(application),ZipFetcher.FetchZipAsyncCompleteListener,PhotoFetcher.FetchPhotosAsyncCompleteListener,AppsFetcher.FetchAppsAsyncCompleteListener,DocumentFetcher.FetchDocumentsAsyncCompleteListener,
    AudioFetcher.FetchAudioAsyncCompleteListener,VideoFetcher.FetchVideoAsyncCompleteListener,GroupVideoPhotoAsyncTask.RecentFetchAsyncCompleteListener {

    var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var videos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var photos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var apps: MutableLiveData<ArrayList<ListItem>> = MutableLiveData()
    var zip_files: MutableLiveData<List<ListItem>> = MutableLiveData()
    var documents: MutableLiveData<List<ListItem>> = MutableLiveData()
//    var audioSize = MutableLiveData<Long>()
//    var videoSize = MutableLiveData<Long>()
//    var photoSize = MutableLiveData<Long>()
    var recent_files : MutableLiveData<RecentUpdatedFiles> = MutableLiveData()
    private val sharedPrefFile = "com.example.new_file_manager"
    var last_login_time :Long ?=0L
    var count_audios:MutableLiveData<Int> = MutableLiveData()
    var count_videos:MutableLiveData<Int> = MutableLiveData()
    var count_images:MutableLiveData<Int> = MutableLiveData()
    var count_documents:MutableLiveData<Int> = MutableLiveData()
    var count_apps:MutableLiveData<Int> = MutableLiveData()
    var count_zip:MutableLiveData<Int> = MutableLiveData()






    fun fetchRecent(context: Activity){
        GroupVideoPhotoAsyncTask(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchAudios(context: Context){
        AudioFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
         last_login_time =sharedPreferences?.getLong("LAST_LOGIN",0L)
        Log.d("countoffiles","$last_login_time empty")

    }

    fun fetchVideos(context: Context){
        VideoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
         last_login_time =sharedPreferences?.getLong("LAST_LOGIN",0L)
    }

    fun fetchImages(context: Context){
        PhotoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
         last_login_time=sharedPreferences?.getLong("LAST_LOGIN",0L)
    }

    fun fetchApps(context: Context){
        AppsFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
         last_login_time=sharedPreferences?.getLong("LAST_LOGIN",0L)




    }

    fun fetchDocuments(context: Context){
        DocumentFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
         last_login_time=sharedPreferences?.getLong("LAST_LOGIN",0L)
    }
     fun fetchZip(context: Context){
            ZipFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
         val sharedPreferences: SharedPreferences? = context.getSharedPreferences(sharedPrefFile,
             Context.MODE_PRIVATE)
          last_login_time=sharedPreferences?.getLong("LAST_LOGIN",0L)
        }


    override fun fetchAudioCompleted(audiosList: List<ListItem>?) {
        audios?.postValue(audiosList)
        var count =0
        for (values in audiosList!!){

            if (values.modified > last_login_time!!){
                 count++

            }
        }
        count_audios.value = count
       // this.audioSize.value = audiosSize
    }

    override fun fetchVideoCompleted(videosList:List<ListItem>?) {

        videos?.postValue(videosList)
        var count =0
        for (values in videosList!!){

            if (values.modified > last_login_time!!){
                count++

            }
        }

        count_videos.value = count
        //this.videoSize.value = videosSize
    }

    override fun fetchPhotosCompleted(photosList: List<ListItem>?) {

        photos?.postValue(photosList)
        var count =0
        last_login_time= last_login_time!! *1000
        Log.d("imahe","$last_login_time last time")
        for (values in photosList!!){

            Log.d("imahe","${values.modified} ")
            if (values.modified > last_login_time!!){
                count++
             }
        }

        count_images.value = count
        Log.d("imahe","$count")
        //this.photoSize.value = photosSize
    }

    override fun fetchAppsCompleted(audiosList: ArrayList<ListItem>?) {
        apps.value = audiosList
        var count =0
        for (values in audiosList!!){

            if (values.modified > last_login_time!!){
                count++


            }
        }

        count_apps.value = count
    }

    override fun fetchDocumentsCompleted(documentsList: ArrayList<ListItem>?) {
        documents.value = documentsList
        var count =0
        for (values in documentsList!!){

            if (values.modified > last_login_time!!){
                count++


            }
        }

        count_documents.value = count
    }

    override fun fetchZipCompleted(zipList: ArrayList<ListItem>?) {
        zip_files.value = zipList
        var count =0
        for (values in zipList!!){

            if (values.modified > last_login_time!!){
                count++


            }
        }

        count_zip.value = count
    }

    override fun recentFetchCompleted(recentList: RecentUpdatedFiles) {
        recent_files.value = recentList
    }



}
