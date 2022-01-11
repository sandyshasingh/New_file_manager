package com.simplemobiletools.filemanager.pro

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import com.simplemobiletools.commons.AppThemePrefrences
import com.simplemobiletools.commons.ThemeUtils
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import kotlinx.android.synthetic.main.activity_settings_burger.*

class SettingsBurger : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.onActivityCreateSetTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_burger)


        back.setOnClickListener {
            super.onBackPressed()
        }

        legal.setOnClickListener {
            val intent = Intent(this, Legal::class.java).apply {

            }
            startActivity(intent)
        }

        about_us.setOnClickListener {
            try {
                val intent =  Intent(Intent.ACTION_VIEW,Uri.parse(WEB_SITE_LINK))
                startActivity(intent)
            }catch (exception: Exception) {
            }

        }

        feedback.setOnClickListener {
            try {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", GMAIL_ID, null
                    )
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "File Manager feedback")
// emailIntent.putExtra(Intent.EXTRA_TEXT, nUrl);
// emailIntent.putExtra(Intent.EXTRA_TEXT, nUrl);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "App Version "+getString(R.string.app_ver))
                startActivity(Intent.createChooser(emailIntent, "Send email..."))

            }catch (exception: Exception) {
            }

//            val intent = Intent(this, Feedback::class.java).apply {
//
//            }
//            startActivity(intent)

        }

        more_apps.setOnClickListener {
            try {
                val intent =  Intent(Intent.ACTION_VIEW,Uri.parse(MORE_APPS_LINK))
                startActivity(intent)
            }catch (exception: Exception) {
            }

        }
        twitter.setOnClickListener {
            try {
                val intent =  Intent(Intent.ACTION_VIEW,Uri.parse(TWITTER_LINK))
                startActivity(intent)
            }catch (exception: Exception) {
            }

        }
        facebook.setOnClickListener {
            try {
                val intent =  Intent(Intent.ACTION_VIEW,Uri.parse(FB_LINK2))
                startActivity(intent)
            }catch (exception: Exception) {
            }

        }
        instagram.setOnClickListener {
            try {
                val intent =  Intent(Intent.ACTION_VIEW,Uri.parse(INSTA_LINK))
                startActivity(intent)
            }catch (exception: Exception) {
            }

        }
       var asa= AppThemePrefrences.GetBooleanSharedPreference(this, ThemeUtils.NIGHT_MODE)
        dark_mode?.isChecked = asa
        dark_mode.setOnCheckedChangeListener{ compoundButton: CompoundButton, isDarkMode: Boolean ->
                AppThemePrefrences.SetBooleanSharedPreference(this, ThemeUtils.NIGHT_MODE,isDarkMode)
                val intent = Intent(this, FileManagerMainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                finish()
//            AppCompatDelegate
//                .setDefaultNightMode(
//                    AppCompatDelegate
//                        .MODE_NIGHT_YES)
//            var nightMode:Boolean = AppThemePrefrences.SetBooleanSharedPreference(activity,
//                ThemeUtils.NIGHT_MODE
//            );
        }


    }
}