package com.simplemobiletools.filemanager.pro.helpers

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.commons.helpers.timeConversionInMinSec
import com.simplemobiletools.filemanager.pro.models.ListItem

class AudioFetcher(var context: Context,var fetchAudioAsyncCompleteListener: FetchAudioAsyncCompleteListener) : AsyncTask<Void, Void, MutableLiveData<List<ListItem>>>() {

    var audios: MutableLiveData<List<ListItem>>? = MutableLiveData()
    var audiosSize = 0L

    override fun doInBackground(vararg p0: Void?):MutableLiveData<List<ListItem>>? {
         return  getAudios(context)
    }
    private fun getAudios(context: Context): MutableLiveData<List<ListItem>>? {

        if(audios?.value == null)
        {
            val songsDataClassList: java.util.ArrayList<ListItem> = java.util.ArrayList()
            val projection = arrayOf(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.ALBUM_ID
            )
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            val query = context?.contentResolver?.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
            )

            query?.use { cursor ->
                Log.d("reached","cursor")

                // Cache column indices.
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val dateId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

                val albumArtUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video
                    val name = cursor.getString(nameColumn)
                    val sizeInLong = cursor.getLong(sizeColumn)
                    val sizeInDouble = cursor.getDouble(sizeColumn)
                    val data = cursor.getString(dataColumn)
                    val durationInMilisec : Int? =  cursor.getInt(durationColumn)
                    val date =  cursor.getLong(dateId)
                    val imageUri= ContentUris.withAppendedId(albumArtUri, cursor.getLong(albumId))

                    if(durationInMilisec!=null)
                    {
                        val duration: String? = timeConversionInMinSec(durationInMilisec)
                        val Size = format(sizeInDouble, 2)
                        Log.d("path",data)
                        if(durationInMilisec>0  && !duration?.equals("00:00")!!) {
                            audiosSize += sizeInLong
                            val item =  ListItem(
                                data,
                                name,
                                false,
                                0,
                                sizeInLong,
                                date,
                                false,
                                imageUri,
                                "",
                                ""
                            )
//                            item.sizeCalculator = totalSize
                            songsDataClassList.plusAssign(item)
                        }
                    }
                }
            }
            audios?.postValue(songsDataClassList)
        }
        //  var list :MutableList<VideoDataClass> = ArrayList()
        return audios
    }

    override fun onPostExecute(result: MutableLiveData<List<ListItem>>?) {
        super.onPostExecute(result)
        fetchAudioAsyncCompleteListener.fetchAudioCompleted(result,audiosSize)
    }
    interface FetchAudioAsyncCompleteListener {
        fun fetchAudioCompleted(audiosList:MutableLiveData<List<ListItem>>?,audiosSize : Long )
    }


}