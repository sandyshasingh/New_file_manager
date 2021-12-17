package com.simplemobiletools.filemanager.pro

import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.commons.helpers.timeConversionInMinSec
import com.simplemobiletools.filemanager.pro.models.ListItem
import java.io.File

class GroupVideoPhotoAsyncTask(var context: Context) : AsyncTask<Void, Void, ArrayList<GroupedDataClass>>() {


    override fun doInBackground(vararg p0: Void?): ArrayList<GroupedDataClass>? {
      val images = getImages()
      //var imagesList =  images?.groupBy {  it.mModified}
      val videos = getVideos(context)

        var joinedNewList = images
        if (videos != null) {
            joinedNewList?.addAll(videos)
        }

      var recentList = joinedNewList?.groupBy { it.mModified }
        Log.d("images","$images  ")
        Log.d("videos","$videos  ")
        Log.d("joinedNewList","$joinedNewList  ")
        Log.d("dates","${recentList?.keys}")
//      val dd=  getVideos()
       // return
        return null
    }

    private val IMAGE_PROJECTION = arrayOf(
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.DISPLAY_NAME,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.DATE_MODIFIED,
        MediaStore.Images.ImageColumns.MIME_TYPE,
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.SIZE,
        MediaStore.Images.ImageColumns.ORIENTATION)

    val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"


    private fun getImages(): ArrayList<ListItem>? {
        val data: ArrayList<ListItem> = java.util.ArrayList<ListItem>()


            var cursor : Cursor? = null
         try {
                cursor = context?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, sortOrder)!!
            } catch (e: Exception) {
            }
            cursor?.use { cursor ->
                val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val imageData = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                val nameColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
                val dateModifiedColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED)
                val mimeTypeColNum = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
                val orientationColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION)
                val fileSize = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColNum)
                    val dateTaken = cursor.getLong(dateTakenColNum)
                    val name = cursor.getString(nameColNum)
                    val mimeType = cursor.getString(mimeTypeColNum)
                    val dateModified = cursor.getLong(dateModifiedColNum)
                    val orientation = cursor.getInt(orientationColNum)
                    val uri = cursor.getString(imageData)
                    val fileSizeValue = cursor.getLong(fileSize)
                    if (uri != null) {
                        val file = File(uri)
                        if (file.exists() && file.length() > 0) {
//                            photosSize += fileSizeValue
                            val photo = ListItem(
                                uri,
                                name,
                                false,
                                0,
                                fileSizeValue,
                                dateTaken,
                                false,
                                null
                            )

                            data.add(photo)
                        }
                    }
                }
            }
        return data
    }

    private fun getVideos(context: Context):ArrayList<ListItem>? {

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
                           // videosSize += sizeInLong
                            songsDataClassList.plusAssign(
                                ListItem(data,
                                    name,
                                    false,
                                    0,
                                    sizeInLong,
                                    date,
                                    false,
                                    null
                                )
                            )
                        }
                    }
                }
            }


        return songsDataClassList
    }

    override fun onPostExecute(result: ArrayList<GroupedDataClass>?) {
        super.onPostExecute(result)

    }

}