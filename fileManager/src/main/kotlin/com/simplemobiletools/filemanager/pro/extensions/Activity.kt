package com.simplemobiletools.filemanager.pro.extensions

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.filemanager.pro.BuildConfig
import com.simplemobiletools.filemanager.pro.helpers.*
import java.io.File
import androidx.core.content.FileProvider
import com.simplemobiletools.commons.MediaStoreData
import com.simplemobiletools.commons.VideoFileInfo

var videoFileInfoArrayList: ArrayList<VideoFileInfo>? = null

fun Activity.sharePaths(paths: ArrayList<String>) {
    sharePathsIntent(paths, "com.example.new_file_manager.provider")
}

fun Activity.tryOpenPathIntent(path: String, forceChooser: Boolean, openAsType: Int = OPEN_AS_DEFAULT) {
    if (!forceChooser && path.endsWith(".apk", true)) {
        val uri = if (isNougatPlus()) {
            FileProvider.getUriForFile(this, "com.example.new_file_manager.provider", File(path))
        } else {
            Uri.fromFile(File(path))
        }
        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, getMimeTypeFromUri(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            } else {
                toast(R.string.no_app_found)
            }
        }
    } else {
        openPath(path, forceChooser, openAsType, this)
    }
}
private fun openPlayerWithUri(path: String, activity: Activity) {
    try {
    /*    val file =File(path)
        val commonFile = VideoFileInfo()
        commonFile.file_name = file.name
        commonFile.uri = Uri.fromFile(file)
        commonFile.file_path = file.absolutePath
        commonFile.createdTime = file.lastModified()

        val cursor = activity.contentResolver.query(Uri.fromFile(file), arrayOf(MediaStore.Video.VideoColumns.BOOKMARK), null, null, null);
        if(cursor!=null) {
            val bookmark = cursor?.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BOOKMARK)
            commonFile.lastPlayedDuration = cursor?.getLong(bookmark)
        }

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        commonFile.fileInfo = FileSpecUtils.getInfo(file.length(), durationStr.toLong(), 1)
        commonFile.isDirectory = false
        commonFile.setFindDuplicate(false)
        val videoList = ArrayList<VideoFileInfo>()
        videoList.add(commonFile)
        if (commonFile.file_path != null) {
            var directoryPath: String? = ""
            if (file.exists()) {
                directoryPath = file.parentFile.path
            }

            if (directoryPath != null && !directoryPath.equals("", ignoreCase = true)) {
               // videoFileInfoArrayList = RootHelper.getVideoFilesListFromFolder(activity, directoryPath, R.array.video, true, false, false) as ArrayList<VideoFileInfo>?
                FetchVideoFileService(activity.applicationContext, commonFile.file_path, directoryPath, false, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            }else {
                val videoIntent = Intent(activity.applicationContext, ExoVideoPlayerActivity::class.java)
                videoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                ExoPlayerDataHolder.setData(videoList)
                videoIntent.putExtra(Constants.EXOPLAYER_VIDEO_ITEM_INDEX, 0)
                videoIntent.putExtra("DURATION", 0)
                activity.startActivity(videoIntent)
            }
        }*/

      //  activity.finish()
    } catch (e: Exception) {
        Log.e("Exception", e.toString())
    }
}

fun Activity.openPath(path: String, forceChooser: Boolean, openAsType: Int = OPEN_AS_DEFAULT, activity: Activity) {
   // openPathIntent(path, forceChooser, "com.rocks.music.videoplayer.provider", getMimeType(openAsType))
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, "com.example.new_file_manager.provider") ?: return@ensureBackgroundThread
        val mimeType = if (getMimeType(openAsType).isNotEmpty())
            getMimeType(openAsType)
        else {
            getUriMimeType(path, newUri)
        }
        Intent().apply {
           /* if (mimeType.contains("video")) {
                openPlayerWithUri(path, activity)
            } else if(mimeType.contains("image")) {
                val bucketArray = arrayOf("" + getImageBucketIDFromURI(activity, newUri))
                openImageViewer(activity, path, mimeType, bucketArray)
            } else if(mimeType.contains("audio")) {
                openMusicPlayer(activity, newUri, path)
            }*/
           /* else {*/
                action = Intent.ACTION_VIEW
                setDataAndType(newUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(REAL_FILE_PATH, path)

                if (resolveActivity(packageManager) != null) {
                    val chooser = Intent.createChooser(this, getString(R.string.open_with))
                    try {
                        startActivity(if (forceChooser) chooser else this)
                    } catch (e: NullPointerException) {
                        showErrorToast(e)
                    }
                } else {
                    if (!tryGenericMimeType(this, mimeType, newUri)) {
                        toast(R.string.no_app_found)
                    }
                }
          //  }

        }
    }
}
fun Activity.findType(path: String) :String {
        val newUri = getFinalUriFromPath(path, "com.example.new_file_manager.provider")

        val mimeType = if (getMimeType(OPEN_AS_DEFAULT).isNotEmpty())
            getMimeType(OPEN_AS_DEFAULT)
        else {
            newUri?.let { getUriMimeType(path, it) }
        }
    return mimeType.toString()
}
fun Activity.openWith(path : String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, "com.example.new_file_manager.provider") ?: return@ensureBackgroundThread
        val mimeType = if (getMimeType(OPEN_AS_DEFAULT).isNotEmpty())
            getMimeType(OPEN_AS_DEFAULT)
        else {
            getUriMimeType(path, newUri)
        }
        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(newUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(REAL_FILE_PATH, path)

            if (resolveActivity(packageManager) != null) {
                val chooser = Intent.createChooser(this, getString(R.string.open_with))
                try {
                    startActivity(chooser)
                } catch (e: NullPointerException) {
                    showErrorToast(e)
                }
            } else {
                if (!tryGenericMimeType(this, mimeType, newUri)) {
                    toast(R.string.no_app_found)
                }
            }


        }
    }
}
private fun openMusicPlayer(activity: Activity, newUri: Uri, path: String) {
    val cursor = getTrackCursor(activity, newUri)
    if (cursor != null) {
       /* val songList = MusicUtils.getSongListForCursor(cursor)
        val audioId = getAudioIdFromPath(activity, path)
        val position = getPositionOfAudio(songList, audioId)
        MusicUtils.playAll(activity, songList, position)
        val intent = Intent("com.music.rocks.PLAYBACK_NOTIFICATION")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("FROM_MUSIC", true)
        intent.putExtra("IS_SLIDING_OPEN",true)
        activity.startActivity(intent)*/
    }
}

private fun openImageViewer(activity: Activity, imagePath: String, mimeType: String, bucketArray: Array<String>) =
        try {
          /*  val mediaStoreDataArrayList = java.util.ArrayList<MediaStoreData>()
            val file = File(imagePath)
            val mediaStore = MediaStoreData(0L, imagePath, file.length(), mimeType, 0L, file.lastModified(), 0, null)
            mediaStoreDataArrayList.add(mediaStore)
            val data = queryImages(activity, bucketArray) //as ArrayList<MediaStoreData>
            if(data!=null && data?.isNotEmpty() && data?.size > 1) {
                val position = getPositionOfImage(data, imagePath)
                FullScreenPhotos.startFullScreenActivity(activity, FullScreenPhotos::class.java, data, position)
            } else {
                FullScreenPhotos.startFullScreenActivity(activity, FullScreenPhotos::class.java, mediaStoreDataArrayList, 0)
            }
*/
        } catch (e: java.lang.Exception) {
          //  Toasty.error(activity.applicationContext, "Error! Sorry for inconvenience.", Toast.LENGTH_LONG, true).show()
        }



public fun getMimeType(type: Int) = when (type) {
    OPEN_AS_DEFAULT -> ""
    OPEN_AS_TEXT -> "text/*"
    OPEN_AS_IMAGE -> "image/*"
    OPEN_AS_AUDIO -> "audio/*"
    OPEN_AS_VIDEO -> "video/*"
    else -> "*/*"
}

fun Activity.setAs(path: String) {
//    setAsIntent(path, "com.rocks.music.videoplayer.provider")
}

fun BaseSimpleActivity.toggleItemVisibility(oldPath: String, hide: Boolean, callback: ((newPath: String,message:String) -> Unit)? = null) {
    val path = oldPath.getParentPath()
    var message = ""
    var filename = oldPath.getFilenameFromPath()
    if ((hide && filename.startsWith('.')) || (!hide && !filename.startsWith('.'))) {
        message = if(hide)
            "Already hidden"
        else
            "Already not hidden"

        callback?.invoke(oldPath,message)
        return
    }

    filename = if (hide) {
        ".${filename.trimStart('.')}"
    } else {
        filename.substring(1, filename.length)
    }

    val newPath = "$path/$filename"
    if (oldPath != newPath) {
        renameFile(oldPath, newPath,false) {success, useAndroid30Way ->
            callback?.invoke(newPath,"")
        }
    }
}
