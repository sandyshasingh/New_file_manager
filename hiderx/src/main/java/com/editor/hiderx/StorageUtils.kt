package com.editor.hiderx

import android.os.Environment
import android.util.Log
import com.editor.hiderx.fragments.FOLDER_TYPE_AUDIOS
import com.editor.hiderx.fragments.FOLDER_TYPE_DOCUMENTS
import com.editor.hiderx.fragments.FOLDER_TYPE_PHOTOS
import com.editor.hiderx.fragments.FOLDER_TYPE_VIDEOS
import java.io.File
import java.util.concurrent.TimeUnit

const val PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS: String = "Unhide photos"
const val PUBLIC_DIRECTORY_HIDERBACKUP_FOR_VIDEOS: String = "Unhide videos"
const val PUBLIC_DIRECTORY_HIDERBACKUP_FOR_AUDIOS: String = "Unhide audios"
const val PUBLIC_DIRECTORY_HIDERBACKUP_FOR_DOCUMENTS: String = "Unhide documents"
const val PUBLIC_DIRECTORY_HIDERBACKUP_FOR_OTHERS: String = "Unhide OTHERS"
const val PASSWORD_FILE_NAME : String = "file_manager_password.txt"

object StorageUtils {



    const val PRIVATE_DIR = "dont_delete_hiderx_files"
    const val offset = 17

    fun decode(enc: String, offset: Int): String? {
        return encode(enc, 26 - offset)
    }

    fun getFileNameFromPath(path: String): String? {
        return try {
            path.substring(path.lastIndexOf("/") + 1)
        } catch (e: java.lang.Exception) {
            "video_file" + System.currentTimeMillis()
        }
    }

    fun getPublicAlbumStorageDirForPhotos(): File? {
        // Get the directory for the app's private pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS)
        if (!file.mkdirs()) {
            Log.e("@STORAGE", "Directory not created")
        }
        return file
    }


    fun getPublicAlbumStorageDirForOthers(): File? {
        // Get the directory for the app's private pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PUBLIC_DIRECTORY_HIDERBACKUP_FOR_OTHERS)
        if (!file.mkdirs()) {
            Log.e("@STORAGE", "Directory not created")
        }
        return file
    }

    fun getDefaultDirectoryForType(currentType: Int): String? {
        when (currentType) {
            FOLDER_TYPE_AUDIOS -> {
                return getPublicAlbumStorageDirForAudios()?.path!!
            }
            FOLDER_TYPE_PHOTOS -> {
                return getPublicAlbumStorageDirForPhotos()?.path!!
            }
            FOLDER_TYPE_VIDEOS -> {
                return getPublicAlbumStorageDirForVideos()?.path!!
            }
            FOLDER_TYPE_DOCUMENTS ->
            {
                return getPublicAlbumStorageDirForDocuments()?.path!!
            }
            else -> {
                return getPublicAlbumStorageDirForOthers()?.path!!
            }
        }

    }

    fun getPublicAlbumStorageDirForVideos(): File? {
        // Get the directory for the app's private pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PUBLIC_DIRECTORY_HIDERBACKUP_FOR_VIDEOS)
        if (!file.mkdirs()) {
            Log.e("@STORAGE", "Directory not created")
        }
        return file
    }

    fun getPublicAlbumStorageDirForAudios(): File? {
        // Get the directory for the app's private pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PUBLIC_DIRECTORY_HIDERBACKUP_FOR_AUDIOS)
        if (!file.mkdirs()) {
            Log.e("@STORAGE", "Directory not created")
        }
        return file
    }

    fun getPublicAlbumStorageDirForDocuments(): File? {
        // Get the directory for the app's private pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PUBLIC_DIRECTORY_HIDERBACKUP_FOR_DOCUMENTS)
        if (!file.mkdirs()) {
            Log.e("@STORAGE", "Directory not created")
        }
        return file
    }

    fun encode(enc: String, offset: Int): String? {
        var offset = offset
        offset = offset % 26 + 26
        val encoded = StringBuilder()
        for (i in enc.toCharArray()) {
            if (Character.isLetter(i)) {
                if (Character.isUpperCase(i)) {
                    encoded.append(('A'.toInt() + (i - 'A' + offset) % 26).toChar())
                } else {
                    encoded.append(('a'.toInt() + (i - 'a' + offset) % 26).toChar())
                }
            } else {
                encoded.append(i)
            }
        }
        return encoded.toString()
    }



    fun move(fromPath: String, toPath: String): Boolean {
        return rename(fromPath, toPath)
    }

    fun getPhotosHiderDirectory(): String {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR + "/.PrivateData" + "/Photos")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory.path
    }

    fun getRootHiderDirectory(): File{
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR)
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory
    }

    fun getHiderDirectory(): File {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + PRIVATE_DIR + "/.PrivateData")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory
    }

    fun getVideosHiderDirectory(): String {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR + "/.PrivateData" + "/Videos")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory.path
    }

    fun getAudiosHiderDirectory(): String {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR + "/.PrivateData" + "/Audios")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory.path
    }

    fun getDocumentsHiderDirectory(): String {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR + "/.PrivateData" + "/Documents")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory.path
    }

    fun getOthersHiderDirectory(): String {
        val parentDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + StorageUtils.PRIVATE_DIR + "/.PrivateData" + "/Others")
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs()
        }
        return parentDirectory.path
    }




    fun format(bytes: Double, digits: Int): String {
        var byte = bytes
        val dictionary = arrayOf("bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
        var index = 0
        while (index < dictionary.size) {
            if (byte < 1024) {
                break
            }
            byte /= 1024
            index++
        }
        return String.format("%." + digits + "f", byte) + " " + dictionary[index]
    }

    fun timeConversionInMinSec(millis: Long): String? {

        return if(millis>=3600000){
            timeConversionInHHMMSS(millis)
        }else {
            /* if(millis ==0L){
                 return ""
             }

              String.format("%02d:%02d", min, sec)*/
            String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            )
        }
    }

    fun timeConversionInHHMMSS(millis: Long) : String{
        return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )

    }

    fun rename(fromPath: String, toPath: String): Boolean {
        return try {
            val file: File =File(fromPath)
            val newFile = File(toPath)
            file.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    fun getFileRootDirectory(): File {
        return Environment.getExternalStorageDirectory()
    }

    fun getPasswordFilePath(): String {
         val pswdFilePath = getHiderDirectory().path+"/$PASSWORD_FILE_NAME"
         File(pswdFilePath).createNewFile()
        return pswdFilePath
    }


}