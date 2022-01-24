package com.simplemobiletools.filemanager.pro.helpers

import android.content.Context
import android.os.AsyncTask
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.simplemobiletools.commons.ListItem
import java.io.File
import java.util.*

class PhotoFetcher(var context: Context, var fetchAudioAsyncCompleteListener: FetchPhotosAsyncCompleteListener) : AsyncTask<Void, Void,List<ListItem>>() {


    var photosSize = 0L


    override fun doInBackground(vararg p0: Void?):List<ListItem>? {
         return  getImages()
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

    private fun getImages():List<ListItem>? {
        val data: MutableList<ListItem> = ArrayList<ListItem>()


            try {
                val cursor = context?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, sortOrder)!!
                cursor.use { cursor ->
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
                                photosSize += fileSizeValue
                                val photo = ListItem(
                                    uri,
                                    name,
                                    false,
                                    0,
                                    fileSizeValue,
                                    dateTaken,
                                    false,
                                    null,
                                    "",
                                    ""
                                )

                                data.add(photo)
                            }
                        }
                    }
                }
                return data
            } catch (e: Exception) {
                return data
            }


    }

    override fun onPostExecute(result:List<ListItem>?) {
        super.onPostExecute(result)
        fetchAudioAsyncCompleteListener.fetchPhotosCompleted(result)
    }
    interface FetchPhotosAsyncCompleteListener {
        fun fetchPhotosCompleted(photosList: List<ListItem>?)
    }


}