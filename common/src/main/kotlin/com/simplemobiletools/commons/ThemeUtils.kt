package com.simplemobiletools.commons

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.simplemobiletools.commons.ThemeUtils.GetBooleanSharedPreference
import java.util.*

object ThemeUtils {

    const val NIGHT_MODE = "NIGHT_MODE"
    const val THEME = "THEME"
    const val THEME_DEFAULT = 0

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

    fun GetBooleanSharedPreference(ctx: Context, Key: String?,defaultValue : Boolean): Boolean {
        val pref: SharedPreferences =
            ctx.getSharedPreferences("com.example.new_file_manager", Context.MODE_PRIVATE)
        return if (pref.contains(Key)) {
            pref.getBoolean(Key, defaultValue)
        } else defaultValue
    }

    fun onActivityCreateSetTheme(activity: AppCompatActivity?)
    {
        val nightMode = AppThemePrefrences.GetBooleanSharedPreference(activity!!, NIGHT_MODE,false)

        if (nightMode){

            activity?.delegate?.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
            activity?.setTheme(R.style.DarkMode)
            return

        }

        else{
            activity?.delegate?.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
            activity?.setTheme(R.style.AppTheme2)
            return

        }

    }
}
/*


        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            activity.setTheme(R.style.NightModeTheme);
            return;
        }
*/



//
//
//
//        sTheme = AppThemePrefrences.GetIntSharedPreference(activity, ThemeConfig.THEME);
//        premiumTheme = AppThemePrefrences.GetBooleanSharedPreference(activity, ThemeConfig.IS_PREMIUM_THEME,false);
//
//        switch (sTheme) {
//
//            case THEME_DEFAULT:
//            activity.setTheme(R.style.AppTheme0);
//            break;
//            case THEME_WHITE:
//            activity.setTheme(R.style.AppTheme1);
//            break;
//
//            default:
//            activity.setTheme(R.style.AppTheme0);
//            break;
//        }
//    }

