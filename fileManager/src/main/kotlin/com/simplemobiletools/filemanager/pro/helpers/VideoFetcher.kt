package com.simplemobiletools.filemanager.pro.helpers

import android.content.Context
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.commons.helpers.timeConversionInMinSec
import com.simplemobiletools.filemanager.pro.models.ListItem

class VideoFetcher(var context: Context, var fetchAudioAsyncCompleteListener: FetchVideoAsyncCompleteListener) : AsyncTask<Void, Void, MutableLiveData<List<ListItem>>>() {

    var videos: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var videosSize = 0L

    override fun doInBackground(vararg p0: Void?):MutableLiveData<List<ListItem>>? {
        return  getVideos(context)
    }
    private fun getVideos(context: Context): MutableLiveData<List<ListItem>>? {
        if(videos?.value == null)
        {
            val songsDataClassList: java.util.ArrayList<ListItem> = java.util.ArrayList()
            val projection = arrayOf(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DATE_ADDED
            )
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            val query = context?.contentResolver?.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
            )

            query?.use { cursor ->
                Log.d("reached","cursor")

                // Cache column indices.
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dataColumn: Int = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateId: Int = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)


                while (cursor.moveToNext()) {
                    // Get values of columns for a given video
                    val name = cursor.getString(nameColumn)
                    val sizeInLong = cursor.getLong(sizeColumn)
                    val sizeInDouble = cursor.getDouble(sizeColumn)

                    val data = cursor.getString(dataColumn)
                    val date = cursor.getLong(dateId)

                    val durationInMilisec =  cursor.getInt(durationColumn)

                    if(durationInMilisec!=null)
                    {
                        val duration: String? = timeConversionInMinSec(durationInMilisec)
                        val Size = format(sizeInDouble, 2)

                        if(durationInMilisec>0  && !duration?.equals("00:00")!!) {
                            videosSize += sizeInLong
                            songsDataClassList.plusAssign(
                                    ListItem(
                                        data,
                                        name,
                                        false,
                                        0,
                                        sizeInLong,
                                        date,
                                        false,
                                        null,
                                        "",
                                        ""
                                    )
                            )
                        }
                    }
                }
            }
            videos?.postValue(songsDataClassList)
        }
        return videos
    }

    override fun onPostExecute(result: MutableLiveData<List<ListItem>>?) {
        super.onPostExecute(result)
        fetchAudioAsyncCompleteListener.fetchVideoCompleted(result,videosSize)
    }
    interface FetchVideoAsyncCompleteListener {
        fun fetchVideoCompleted(videosList: MutableLiveData<List<ListItem>>?, videosSize: Long)
    }

}