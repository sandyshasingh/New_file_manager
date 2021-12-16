package com.simplemobiletools.commons

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.lang.StringBuilder

object MemorySizeUtils {

    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    private fun TotalSDMemory(): Long {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.absolutePath)
        val blockSize = stat.blockSize.toLong()
        val totalBlocks = stat.blockCount.toLong()
        val totalSpace = totalBlocks * blockSize
        Log.d(TAG, "Size of total SD Memory: $totalSpace")
        Log.d(TAG, "External storage emulated: " + Environment.isExternalStorageEmulated())
        return totalSpace
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getAvailableInternalMemorySize(): String? {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return formatSize(availableBlocks * blockSize)
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getTotalInternalMemorySize(): String? {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)


    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    fun getTotalInternalMemorySizeForMe(): String? {
//        val path = Environment.getDataDirectory()
//        val stat = StatFs(path.path)
//        val blockSize = stat.blockSizeLong
//        val totalBlocks = stat.blockCountLong
//        return totalBlocks * blockSize
//    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getAvailableInternalMemorySizeInLong(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getTotalInternalMemorySizeInLong(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getAvailableExternalMemorySize(): String? {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            formatSize(availableBlocks * blockSize)
        } else {
            return null
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getTotalExternalMemorySize(): String? {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            formatSize(totalBlocks * blockSize)
        } else {
            return null
        }
    }

    fun round(value: Double, places: Int): Double {
        var value = value
        require(places >= 0)
        val factor = Math.pow(10.0, places.toDouble()).toLong()
        value *= factor
        val tmp = Math.round(value)
        return tmp.toDouble() / factor
    }

    fun formatSize(size1: Long): String? {
        var suffix: String? = null
        var size = size1.toDouble()
        if (size >= 1024) {
            suffix = " KB"
            size /= 1024.0
            if (size >= 1024) {
                suffix = " MB"
                size /= 1024.0
                if (size >= 1024) {
                    suffix = " GB"
                    size /= 1024.0
                }
            }
        }
        size = round(size, 2)
        val resultBuffer = StringBuilder(size.toString())

        /*  int commaOffset = resultBuffer.length() - 3;
    while (commaOffset > 0) {
        resultBuffer.insert(commaOffset, ',');
        commaOffset -= 3;
    }*/if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }


}