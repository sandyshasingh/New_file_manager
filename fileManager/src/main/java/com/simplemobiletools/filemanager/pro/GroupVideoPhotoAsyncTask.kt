package com.simplemobiletools.filemanager.pro

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import com.simplemobiletools.commons.ListItem
import com.simplemobiletools.commons.helpers.format
import com.simplemobiletools.commons.helpers.timeConversionInMinSec
import java.io.File
import java.text.SimpleDateFormat

class GroupVideoPhotoAsyncTask(
    var context: Activity,
    var recentFetchAsyncCompleteListener: RecentFetchAsyncCompleteListener
) : AsyncTask<Void, Void, RecentUpdatedFiles>() {

    var recentfileDataClassList: ArrayList<GroupedDataClass> = ArrayList()
   // private var mProgressDialog: AppProgressDialog? = null

//    override fun onPreExecute() {
//        super.onPreExecute()
//        //showDialog()
//    }

    override fun doInBackground(vararg p0: Void?):RecentUpdatedFiles {
        val images = getImages()
        //var imagesList =  images?.groupBy {  it.mModified}
        val videos = getVideos(context)

        var joinedNewList = images
        if (videos != null) {
            joinedNewList?.addAll(videos)
//            joinedNewList?.sorted()
        }

        var recentList = joinedNewList?.groupBy { it.dateModifiedInFormat }
        Log.d("images", "$images  ")
        Log.d("videos", "$videos  ")
        Log.d("joinedNewList", "$joinedNewList  ")
        Log.d("dates", "${recentList?.keys}")
        val valuess = (recentList?.values)!!.toList()
        val keyss =  (recentList?.keys)!!.toList()

        /* for(i in recentList?.keys!!)
         {
             val hoho = GroupedDataClass(recentList?.get(i)!!,i,)
         }*/
//      val dd=  getVideos()

        // return
        return RecentUpdatedFiles(keyss,valuess)
    }
//    private fun showDialog() {
//        try {
//            //Dismiss if already exist
////            dismissDialog()
//            if (ThemeUtils.getActivityIsAlive(context)) {
//                mProgressDialog = AppProgressDialog(context)
//                mProgressDialog?.setCancelable(true)
//                mProgressDialog?.setCanceledOnTouchOutside(true)
//                mProgressDialog?.show()
//            }
//        } catch (e: Exception) {
//        }
//    }

    private val IMAGE_PROJECTION = arrayOf(
        MediaStore.Images.ImageColumns._ID,
        MediaStore.Images.ImageColumns.DISPLAY_NAME,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns.DATE_MODIFIED,
        MediaStore.Images.ImageColumns.MIME_TYPE,
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.SIZE,
        MediaStore.Images.ImageColumns.ORIENTATION
    )

    val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"


    private fun getImages(): ArrayList<ListItem>? {
        val data: ArrayList<ListItem> = java.util.ArrayList<ListItem>()


        var cursor: Cursor? = null
        try {
            cursor = context?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGE_PROJECTION,
                null,
                null,
                sortOrder
            )!!
        } catch (e: Exception) {
        }
        cursor?.use { cursor ->
            val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            val imageData = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
            val nameColNum =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
            val dateTakenColNum =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
            val dateModifiedColNum =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED)
            val mimeTypeColNum = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
            val orientationColNum =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION)
            val fileSize = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColNum)
                val dateTaken = cursor.getLong(dateTakenColNum)
                val name = cursor.getString(nameColNum)
                val mimeType = cursor.getString(mimeTypeColNum)
                val dateModified = cursor.getLong(dateModifiedColNum)
                val orientation = cursor.getInt(orientationColNum)


//                var formatter:SimpleDateFormat("dd/MM/yyyy")
//                String dateString = formatter.format(new Date(dateInMillis)));

                val uri = cursor.getString(imageData)
                val fileSizeValue = cursor.getLong(fileSize)
                if (uri != null) {
                    val file = File(uri)
                    if (file.exists() && file.length() > 0) {
//                            photosSize += fileSizeValue

                        val df = SimpleDateFormat("dd/MM/yyyy")
                        val dateModifiedInFormat = if (file.lastModified() > 0)
                            df.format(file.lastModified())
                        else
                            df.format(dateTaken)
                        val photo = ListItem(
                            uri,
                            name,
                            false,
                            0,
                            fileSizeValue,
                            dateTaken,
                            false,
                            null, dateModifiedInFormat, "image/*"
                        )

                        data.add(photo)
                    }
                }
            }
        }
        return data
    }

    private fun getVideos(context: Context): ArrayList<ListItem>? {

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
            Log.d("reached", "cursor")

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

                val file = File(data)

                val df = SimpleDateFormat("dd/MM/yyyy")
                val dateModifiedInFormat = if (file.lastModified() > 0)
                    df.format(file.lastModified())
                else
                    df.format(file.lastModified())
//                val df = SimpleDateFormat("dd/MM/yyyy")
//                val dateModifiedInFormat = if (date > 0)
//                    df.format(date)
//                else
//                { df.format(date)}

                val durationInMilisec = cursor.getInt(durationColumn)

                if (durationInMilisec != null) {
                    val duration: String? = timeConversionInMinSec(durationInMilisec)
                    val Size = format(sizeInDouble, 2)

                    if (durationInMilisec > 0 && !duration?.equals("00:00")!!) {
                        // videosSize += sizeInLong
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
                                dateModifiedInFormat,"video/*"
                            )
                        )
                    }
                }
            }
        }


        return songsDataClassList
    }

    override fun onPostExecute(result: RecentUpdatedFiles) {
        super.onPostExecute(result)
        recentFetchAsyncCompleteListener.recentFetchCompleted(result)
        //mProgressDialog?.dismiss()

    }

    interface RecentFetchAsyncCompleteListener {
        fun recentFetchCompleted(audiosList: RecentUpdatedFiles)
    }


}