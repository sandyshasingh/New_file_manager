package com.simplemobiletools.filemanager.pro.helpers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.filemanager.pro.AppDataHolder
import com.simplemobiletools.filemanager.pro.GroupVideoPhotoAsyncTask
import com.simplemobiletools.filemanager.pro.GroupedDataClass
import com.simplemobiletools.filemanager.pro.RecentUpdatedFiles
import com.simplemobiletools.filemanager.pro.models.ListItem

class DataViewModel(application: Application):  AndroidViewModel(application),ZipFetcher.FetchZipAsyncCompleteListener,PhotoFetcher.FetchPhotosAsyncCompleteListener,AppsFetcher.FetchAppsAsyncCompleteListener,DocumentFetcher.FetchDocumentsAsyncCompleteListener,
    AudioFetcher.FetchAudioAsyncCompleteListener,VideoFetcher.FetchVideoAsyncCompleteListener,GroupVideoPhotoAsyncTask.RecentFetchAsyncCompleteListener {

    var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var videos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var photos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var apps: MutableLiveData<ArrayList<ListItem>> = MutableLiveData()
    var zip_files: MutableLiveData<List<ListItem>> = MutableLiveData()
    var documents: MutableLiveData<List<ListItem>> = MutableLiveData()
    var audioSize = MutableLiveData<Long>()
    var videoSize = MutableLiveData<Long>()
    var photoSize = MutableLiveData<Long>()
    var recent_files : MutableLiveData<RecentUpdatedFiles> = MutableLiveData()

    fun fetchRecent(context: Activity){
        GroupVideoPhotoAsyncTask(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchAudios(context: Context){
        AudioFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchVideos(context: Context){
        VideoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchImages(context: Context){
        PhotoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchApps(context: Context){
        AppsFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchDocuments(context: Context){
        DocumentFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
     fun fetchZip(context: Context){
            ZipFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }


    override fun fetchAudioCompleted(audiosList: MutableLiveData<List<ListItem>>?, audiosSize: Long) {
        audios = audiosList
        this.audioSize.value = audiosSize
    }

    override fun fetchVideoCompleted(videosList: MutableLiveData<List<ListItem>>?, videosSize: Long) {
        videos = videosList
        this.videoSize.value = videosSize
    }

    override fun fetchPhotosCompleted(photosList: MutableLiveData<List<ListItem>>?, photosSize: Long) {
        photos = photosList
        this.photoSize.value = photosSize
    }

    override fun fetchAppsCompleted(audiosList: ArrayList<ListItem>?) {
        apps.value = audiosList
    }

    override fun fetchDocumentsCompleted(documentsList: ArrayList<ListItem>?) {
        documents.value = documentsList
    }

    override fun fetchZipCompleted(zipList: ArrayList<ListItem>?) {
        zip_files.value = zipList    }

    override fun recentFetchCompleted(recentList: RecentUpdatedFiles) {
        recent_files.value = recentList
    }



}
