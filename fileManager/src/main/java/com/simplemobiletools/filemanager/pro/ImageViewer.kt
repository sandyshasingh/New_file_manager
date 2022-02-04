package com.simplemobiletools.filemanager.pro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.simplemobiletools.commons.PhotoViewer

class ImageViewer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)


        var position = intent.getIntExtra("pos", 0)

        supportFragmentManager.beginTransaction().add(
            R.id.holder_fragment, PhotoViewer.newInstance(
                position
            )
        ).commit()

    }
}