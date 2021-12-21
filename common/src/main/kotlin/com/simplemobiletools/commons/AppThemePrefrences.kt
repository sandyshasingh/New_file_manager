package com.simplemobiletools.commons

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class AppThemePrefrences {

    companion object
    {
        fun SetSharedPreference(ctx: Context, Key: String?, Value: String?) {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putString(Key, Value)
            editor.apply()
        }


        fun SetBooleanSharedPreference(ctx: Context, Key: String?, Value: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(ctx)
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putBoolean(Key, Value)
            editor.apply()
        }

        fun SetBooleanSharedPreferenceMultiProcess(ctx: Context, Key: String?, Value: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(ctx)
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_MULTI_PROCESS)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putBoolean(Key, Value)
            editor.apply()
        }

        fun GetBooleanSharedPreference(ctx: Context, Key: String?): Boolean {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getBoolean(Key, false)
            } else false
        }


        fun GetBooleanSharedPreferenceMultiProcess(ctx: Context, Key: String?): Boolean {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_MULTI_PROCESS)
            return if (pref.contains(Key)) {
                pref.getBoolean(Key, false)
            } else false
        }

        fun GetBooleanSharedPreference(ctx: Context, Key: String?, defaultvalue: Boolean): Boolean {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getBoolean(Key, defaultvalue)
            } else defaultvalue
        }


        fun SetLongSharedPreference(ctx: Context, Key: String?, Value: Long?) {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            if (Value != null) {
                editor.putLong(Key, Value)
            }
            editor.apply()
        }

        fun GetLongSharedPreference(ctx: Context, Key: String?): Long {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getLong(Key, 0)
            } else 0
        }


        fun GetSharedPreference(ctx: Context, Key: String?): String? {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getString(Key, "")
            } else null
        }


        fun SetIntSharedPreference(ctx: Context, Key: String?, Value: Int) {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putInt(Key, Value)
            editor.apply()
        }

        fun SetFloatSharedPreference(ctx: Context, Key: String?, Value: Float) {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putFloat(Key, Value)
            editor.apply()
        }

        fun GetIntSharedPreference(ctx: Context, Key: String?, defaultValue: Int): Int {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getInt(Key, defaultValue)
            } else defaultValue
        }


        fun GetIntSharedPreference(ctx: Context, Key: String?): Int {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getInt(Key, 0)
            } else 0
        }


        fun GetFloatSharedPreference(ctx: Context, Key: String?): Float {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getFloat(Key, 0.0f)
            } else 0.0f
        }

        fun GetFloatSharedPreference(ctx: Context, Key: String?, defaultValue: Float): Float {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getFloat(Key, defaultValue)
            } else defaultValue
        }


        fun GetSharedPreferenceWithDefaultValue(
            ctx: Context,
            Key: String?,
            defaultValue: String?
        ): String? {
            val pref: SharedPreferences =
                ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
            return if (pref.contains(Key)) {
                pref.getString(Key, defaultValue)
            } else defaultValue
        }
    }

}