package com.simplemobiletools.filemanager.pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.simplemobiletools.commons.AppThemePrefrences
import com.simplemobiletools.commons.ThemeUtils
import kotlinx.android.synthetic.main.activity_settings_burger.*

class SettingsBurger : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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

        night_mode.setOnClickListener {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_YES)
            var nightMode:Boolean = AppThemePrefrences.SetBooleanSharedPreference(activity,
                ThemeUtils.NIGHT_MODE
            );
        }


    }
}