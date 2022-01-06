package com.simplemobiletools.filemanager.pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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


    }
}