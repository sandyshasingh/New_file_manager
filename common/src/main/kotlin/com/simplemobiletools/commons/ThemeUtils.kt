package com.simplemobiletools.commons

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateFormat
import java.util.*

object ThemeUtils {

    const val NIGHT_MODE = "NIGHT_MODE"
    const val THEME = "THEME"

    fun getActivityIsAlive(activity: Activity?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity != null && !activity.isDestroyed
        } else {
            activity != null && !activity.isFinishing
        }
    }

    fun GetBooleanSharedPreference(ctx: Context, Key: String?): Boolean {
        val pref: SharedPreferences =
            ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getBoolean(Key, true)
        } else true
    }

    fun onActivityCreateSetTheme(activity: Activity?)
    {

    }

}