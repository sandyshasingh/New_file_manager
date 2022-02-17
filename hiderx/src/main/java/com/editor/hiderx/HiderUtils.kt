package com.editor.hiderx

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.editor.hiderx.fragments.APPLICATION_ID
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.dialog_help.view.*
import java.io.File

const val VERSION_CODE_FOR_VIDEO_PLAYER = 351
const val ACTION_FOR_VIDEO_PLAYER = "com.rocks.music.videoplayer.DeeplinkActivity"
const val PACKAGE_NAME_FOR_VIDEO_PLAYER = "com.rocks.music.videoplayer"
object HiderUtils {

    const val KEY_FROM_SETUP_PASSWORD = "fromSetUpPassword"
    const val FEEDBACK_EMAIL = "feedback.rocksplayer@gmail.com"
    const val Is_PHOTO_MODE: String = "initPhotoMode"
    const val RATING_THRESHOLD: Byte = 4
    const val PAGER_POSITION : String = "pagerPosition"
    const val TURN_FLASH_OFF_AT_STARTUP: String = "turnFlashOffAtStartup"
    const val FLASH_LIGHT_STATE: String = "flashLightState"
    const val PRIMARY_COLOR_KEY: String = "primaryColor"
    const val FOCUS_BEFORE_CAPTURE: String = "focusBeforeCapture"
    const val ALWAYS_OPEN_BACK_CAMERA : String = "alwaysOpenBackCamera"
    const val PASSWORD_KEY = "PASS_WORD"
    const val KEY_FOR_USER_NAME = "USER_NAME"
    const val FRONT_VIDEO_RES_INDEX_KEY = "frontVideoResIndex"
    const val FRONT_PHOTO_RES_INDEX_KEY = "frontPhotoResIndex"
    const val BACK_VIDEO_RES_INDEX_KEY = "backVideoResIndex"
    const val BACK_PHOTO_RES_INDEX_KEY = "backPhotoResIndex"
    const val LAST_USED_CAMERA_ID = "lastUsedCamera"
    const val LAST_PHOTO_VIDEO_PATH = "lastPhotoVideoPath"
    const val IS_SOUND_ENABLED = "isSoundEnabled"
    const val Last_File_Viewed_Time = "lastFileViewedTime"
    const val Last_File_Insert_Time  = "lastFileInsertTime"
    const val ORIENT_LANDSCAPE_LEFT = 1
    const val ORIENT_LANDSCAPE_RIGHT = 2

    fun getSharedPreference(ctx: Context, Key: String?): String? {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getString(Key, "")
        } else null
    }

    fun setSharedPreference(ctx: Context, Key: String?, Value: String?) {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(Key, Value)
        editor.apply()
    }


    fun setBooleanSharedPreference(ctx: Context, Key: String?, Value: Boolean) {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(Key, Value)
        editor.apply()
    }


    fun sendFeedBack(context: Context, subject: String?, email: String, text: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.Builder().scheme("mailto").build()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        // emailIntent.putExtra(Intent.EXTRA_TEXT, nUrl);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }


    fun getAppVersionName(context: Context): String? {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return ""
    }

    fun getDeviceConfiguration(): String? {
        val myDeviceModel = Build.MODEL
        val myDevice = Build.DEVICE
        val myDeviceDisplay = Build.DISPLAY
        val myDeviceOs = System.getProperty("os.version")
        val myDeviceVersion = Build.VERSION.SDK
        val myDeviceProduct = Build.PRODUCT
        return """
             Modal:-$myDeviceModel
             Device:-$myDevice
             Display:-$myDeviceDisplay
             OS:-$myDeviceOs
             API Level:-$myDeviceVersion
             product:-$myDeviceProduct
             Total memory:-${getUsedMemorySize()?.get(1)}
             Free memory:-${getUsedMemorySize()?.get(0)}
             Used memory:-${getUsedMemorySize()?.get(2)}
             """.trimIndent()
    }


    fun getUsedMemorySize(): LongArray? {
        var freeSize = 0L
        var totalSize = 0L
        var usedSize = -1L
        try {
            val info = Runtime.getRuntime()
            freeSize = info.freeMemory()
            totalSize = info.totalMemory()
            usedSize = totalSize - freeSize
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return longArrayOf(freeSize, totalSize, usedSize)
    }

    fun isDeviceOnline(context: Context?): Boolean {
        if (context != null) {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
        return true
    }

    fun getBooleanSharedPreference(ctx: Context, Key: String?): Boolean {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getBoolean(Key, false)
        } else false
    }


    fun getSharedPreference(ctx: Context, Key: String?, defaultValue: String?): String? {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getString(Key, "")
        } else defaultValue
    }

    fun setIntSharedPreference(ctx: Context, Key: String?, Value: Int) {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(Key, Value)
        editor.apply()
    }

    fun getIntSharedPreference(ctx: Context, Key: String?): Int {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getInt(Key, 0)
        } else 0
    }



    fun setLongSharedPreference(ctx: Context, Key: String?, Value: Long) {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(Key, Value)
        editor.apply()
    }

    fun getLongSharedPreference(ctx: Context, Key: String?): Long {
        val pref = ctx.getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getLong(Key, 0L)
        } else 0
    }

    fun getActivityIsAlive(activity: Activity?): Boolean {
        return activity != null && !activity.isDestroyed
    }

    fun showHelpDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_help, null)
        val dialog1 = AlertDialog.Builder(context)
        dialog1.setView(view)
        val dialog = dialog1.create()
        view.tv_got_it?.setOnClickListener()
        {
            dialog?.dismiss()
        }
        dialog1.setCancelable(true)
        dialog.window?.setBackgroundDrawable(
                ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.custom_dialog_background,
                        null
                )
        )
        dialog?.show()
    }

    private fun isPackageInstalled(s: String, packageManager: PackageManager): Int {
        return try {
            val info = packageManager.getPackageInfo(s, 0)
            if (info.versionCode > VERSION_CODE_FOR_VIDEO_PLAYER) {
                0
            } else if (info.versionCode <= VERSION_CODE_FOR_VIDEO_PLAYER) {
                1
            } else {
                2
            }
        } catch (e: PackageManager.NameNotFoundException) {
            2
        }
    }


    fun playVideo(context: Context, path: String, type: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "$APPLICATION_ID.provider", File(path))
            if (isPackageInstalled(PACKAGE_NAME_FOR_VIDEO_PLAYER,context.packageManager) == 0) {
                try {
                        val intent = Intent(ACTION_FOR_VIDEO_PLAYER)
                         intent.setDataAndType(uri, type)
                            //intent.putExtra("FROM_HIDERX_APP",true)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                }
                catch (e : java.lang.Exception)
                {
                    FirebaseCrashlytics.getInstance().apply {
                        recordException(e)
                        log(e.toString())
                    }
                }
            } else {
                val commonIntent = Intent(Intent.ACTION_VIEW)
                if(uri!=null)
                {
                    commonIntent.setDataAndType(uri, type)
                    commonIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(commonIntent)
                }
                else
                {
                    Toast.makeText(context, "No Apps found to open such a file", Toast.LENGTH_LONG).show()
                }
            }
        }
        catch (e: java.lang.Exception)
        {
            Toast.makeText(context, "No Apps found to open this file", Toast.LENGTH_LONG).show()
        }
    }


}