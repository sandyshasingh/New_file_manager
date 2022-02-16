package com.editor.hiderx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.editor.hiderx.R
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        setListener()
    }

    private fun setListener() {
        tv_start_to_use?.setOnClickListener()
        {
            startActivity(Intent(this, ActivitySetupPasswordHelp::class.java))
            finish()
        }
    }
}