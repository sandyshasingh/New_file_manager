package com.simplemobiletools.filemanager.pro.helpers

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.filemanager.pro.models.ListItem

class DataViewModel(application: Application):  AndroidViewModel(application),PhotoFetcher.FetchPhotosAsyncCompleteListener, AudioFetcher.FetchAudioAsyncCompleteListener,VideoFetcher.FetchVideoAsyncCompleteListener {

    var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var videos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var photos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var zip_files: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var documents: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var applications: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var audioSize = MutableLiveData<Long>()
    var videoSize = MutableLiveData<Long>()
    var photoSize = MutableLiveData<Long>()

    fun fetchAudios(context: Context){
        AudioFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchVideos(context: Context){
        VideoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun fetchImages(context: Context){
        PhotoFetcher(context,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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


}
