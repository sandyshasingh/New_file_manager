package com.simplemobiletools.filemanager.pro.extensions

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.text.format.DateFormat

import com.simplemobiletools.commons.extensions.showErrorToast
import java.io.File
import java.lang.Exception
import java.net.URI
import java.util.*

import android.util.Log
import com.simplemobiletools.commons.MediaStoreData
import com.simplemobiletools.commons.VideoFileInfo
import com.simplemobiletools.commons.convertIntoDate

fun getVideoFilePosition(videoFileInfoArrayList: List<VideoFileInfo>, path: String): Int? {
    val videoListSize = videoFileInfoArrayList.size
    for (x in 0 until videoListSize) {
        if (path == videoFileInfoArrayList[x].file_path) {
            return x
        }
    }
    return 0
}

fun getImageBucketIDFromURI(context: Context, contentUri: Uri?): Long {
    var cursor: Cursor? = null
    return try {
        val fileName: String? = getFileNameFromPath(context, contentUri)
        val proj = arrayOf(MediaStore.Images.Media.BUCKET_ID)
        cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Images.Media.DATA + " like ? ", arrayOf("%$fileName%"), null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
        cursor.moveToFirst()
        cursor.getLong(column_index)
    }catch (e : Exception) {
        Log.d("VIBHOR",e.toString()).toLong()
    }
    /*finally {
        cursor?.close()
    }*/
}
private fun getAudioBucketIDFromURI(context: Context, contentUri: Uri?): Long {
    var cursor: Cursor? = null
    return try {
        val fileName: String? = getFileNameFromPath(context, contentUri)
        val proj = arrayOf(MediaStore.Audio.Media.BUCKET_ID)
        cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media.DATA + " like ? ", arrayOf("%$fileName%"), null)
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID)
        cursor?.moveToFirst()
        cursor?.getLong(column_index!!)!!
    } finally {
        cursor?.close()
    }
}
private val projection2 = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DATE_MODIFIED,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID

)

fun getTrackCursor(activity: Activity, newUri: Uri): Cursor? {
    val selection = MediaStore.Audio.Media.BUCKET_ID + "=?"

    val bucketArray = arrayOf("" + getAudioBucketIDFromURI(activity, newUri))
    return activity.contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection2,
            selection,
            bucketArray,

            MediaStore.Audio.Media.TITLE + " ASC"
    )
}
fun getAudioIdFromPath(activity: Activity, path: String): Long? {
    var songId = 0L
    var id = 0

    val cursor: Cursor? = activity.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection2,
                MediaStore.Audio.Media.DATA + "=?",
                arrayOf(path),
                null)

    while (cursor!=null && cursor?.moveToNext()) {
        try {
            id = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
            songId= cursor?.getLong(id)
        }catch (ex: IllegalArgumentException) {
            id = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            songId= cursor?.getLong(id)
        }
    }
    return songId
}
 fun getAudioImageFromPath(activity: Activity, path: String): Uri? {

     val albumArtUri = Uri.parse("content://media/external/audio/albumart")
     var imageUri: Uri? = null

//    val pathh = File(URI(path).path).canonicalPath
    val cursor: Cursor? = activity.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection2,
            MediaStore.Audio.Media.DATA + "=?",
            arrayOf(path),
            null)

    while (cursor!=null && cursor.moveToNext()) {
        try {
            val albumId: Int = cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            imageUri= ContentUris.withAppendedId(albumArtUri, cursor.getLong(albumId))
        }catch (ex: IllegalArgumentException) {

        }
    }
    return imageUri
}
fun getFileNameFromPath(context: Context, uri: Uri?): String? {
    var result: String? = null
    val scheme = uri?.scheme
    if (scheme == "file") {
        result = uri?.lastPathSegment
    } else if (uri?.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor?.moveToFirst()) {
                result = cursor?.getString(cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor!!.close()
        }
    }
    if (result == null) {
        result = uri?.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun queryImages(activity: Activity, mBucketId: Array<String>?): List<MediaStoreData> {
    val data: ArrayList<MediaStoreData> = java.util.ArrayList()

    val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.ORIENTATION)

    val sortByCol= MediaStore.Images.ImageColumns._ID

    val cursor: Cursor
    cursor = if (mBucketId!=null && mBucketId.isNotEmpty()) {
        val selection = MediaStore.Images.Media.BUCKET_ID + "=?"
        activity.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, selection, mBucketId, "$sortByCol DESC")!!
    } else {
        activity.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, "$sortByCol DESC")!!
    }
    if (cursor == null) {
        return data
    }
    cursor.use { cursor ->
        val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
        val imageData = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
        val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
        val dateModifiedColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED)
        val mimeTypeColNum = cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)
        val orientationColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION)
        val fileSize = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColNum)
            val dateTaken = cursor.getLong(dateTakenColNum)
            val mimeType = cursor.getString(mimeTypeColNum)
            val dateModified = cursor.getLong(dateModifiedColNum)
            val orientation = cursor.getInt(orientationColNum)
            val uri = cursor.getString(imageData)
            val fileSizeValue = cursor.getLong(fileSize)
            val mediaStoreData = MediaStoreData(id, uri, fileSizeValue,
                    mimeType, dateTaken, dateModified, orientation, convertIntoDate(dateTaken),"")
            mediaStoreData.setFindDuplicate(false)

            if (!TextUtils.isEmpty(uri)) {
                val file = File(uri)
                if (file != null && file.exists() && file.length() > 0) {
                    data.add(mediaStoreData)
                }
            }
        }
    }
    return data.reversed()
}

fun queryAudio(activity: Activity, mBucketId: Array<String>): List<MediaStoreData> {
    val data: ArrayList<MediaStoreData> = java.util.ArrayList()
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val IMAGE_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATE_TAKEN,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ORIENTATION)

    val sortByCol= MediaStore.Images.ImageColumns._ID

    val cursor: Cursor
    cursor = if (mBucketId.isNotEmpty()) {
        val selection = MediaStore.Images.Media.BUCKET_ID + "=?"
        activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, selection, mBucketId, "$sortByCol DESC")!!
    } else {
        activity.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, "$sortByCol DESC")!!
    }
    if (cursor == null) {
        return data
    }
    cursor.use { cursor ->
        val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val imageData = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_TAKEN)
        val dateModifiedColNum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
        val mimeTypeColNum = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
        val orientationColNum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ORIENTATION)
        val fileSize = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColNum)
            val dateTaken = cursor.getLong(dateTakenColNum)
            val mimeType = cursor.getString(mimeTypeColNum)
            val dateModified = cursor.getLong(dateModifiedColNum)
            val orientation = cursor.getInt(orientationColNum)
            val uri = cursor.getString(imageData)
            val fileSizeValue = cursor.getLong(fileSize)
            val mediaStoreData = MediaStoreData(id, uri, fileSizeValue,
                    mimeType, dateTaken, dateModified, orientation, convertIntoDate(dateTaken),"")
            mediaStoreData.setFindDuplicate(false)

            if (!TextUtils.isEmpty(uri)) {
                val file = File(uri)
                if (file != null && file.exists() && file.length() > 0) {
                    data.add(mediaStoreData)
                }
            }
        }
    }
    return data
}

fun getPositionOfAudio(songList: LongArray, songId: Long?): Int {
    for (x in songList.indices) {
        if (songList[x]==songId) {
            return x
        }
    }
    return 0
}
fun getPositionOfImage(mediaStoreDatas: List<MediaStoreData>, imagePath: String): Int {
    for (x in mediaStoreDatas.indices) {
        if (mediaStoreDatas[x].uri.contains(imagePath)) {
            return x
        }
    }
    return 0
}

fun getPositionOfImageUsingPath(mediaStoreDatas: List<String>, imagePath: String): Int {
    for (x in mediaStoreDatas.indices) {
        if (mediaStoreDatas[x].contains(imagePath)) {
            return x
        }
    }
    return 0
}