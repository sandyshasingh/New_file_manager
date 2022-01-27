package com.simplemobiletools.filemanager.pro.helpers

import android.content.Context
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.commons.helpers.timeConversionInMinSec

class VideoFetcher(var context: Context, var fetchAudioAsyncCompleteListener: FetchVideoAsyncCompleteListener) : AsyncTask<Void, Void, List<ListItem>>() {

    var videosSize = 0L

    override fun doInBackground(vararg p0: Void?):List<ListItem>? {
        return  getVideos(context)

    }
    private fun getVideos(context: Context): List<ListItem>? {
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
        Log.d("sandy","${songsDataClassList.size}")
        return songsDataClassList

    }

    override fun onPostExecute(result: List<ListItem>) {
        super.onPostExecute(result)
        fetchAudioAsyncCompleteListener.fetchVideoCompleted(result)
    }
    interface FetchVideoAsyncCompleteListener {
        fun fetchVideoCompleted(videosList: List<ListItem>?)
    }

}