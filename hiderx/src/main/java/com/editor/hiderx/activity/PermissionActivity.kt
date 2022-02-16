package com.editor.hiderx.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.editor.hiderx.R
import com.editor.hiderx.Utility
import com.editor.hiderx.doVisible
import kotlinx.android.synthetic.main.activity_permission.*

const val STORAGE_PERMISSION = 23
const val SCHEME = "package"
const val REQUEST_CODE_FOR_SETTINGS_ACTIVITY = 300

class PermissionActivity : AppCompatActivity() {

    private var checkStoragePermission: Boolean = false
    private val storage_permissions =arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    var deniedOnce : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        checkStoragePermission = intent.getBooleanExtra(Utility.KEY_STORAGE_PERMISSION,false)
        if(checkStoragePermission)
        {
                handleStoragePermission()
            tv_allow?.setOnClickListener()
            {
                    val intent = Intent()
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    else
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.parse("$SCHEME:$packageName")
                    intent.data = uri
                    startActivityForResult(
                            intent, REQUEST_CODE_FOR_SETTINGS_ACTIVITY
                    )
            }
        }
    }

    private fun handleStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if(!Environment.isExternalStorageManager())
                startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:$packageName")), REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE)
        }
        else if(!hasStoragePermission())
        {
            ActivityCompat.requestPermissions(this, storage_permissions, STORAGE_PERMISSION)
        }
    }

    private fun hasStoragePermission(): Boolean {
       return ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )== PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE)
        {
            if(Environment.isExternalStorageManager())
            {
                setResult(RESULT_OK)
                finish()
            }
            else
            {
                deniedOnce = true
                tv_allow?.text = "Open Settings"
                tv_open_settings?.doVisible()
            }
        }
        else if(requestCode == REQUEST_CODE_FOR_SETTINGS_ACTIVITY)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                if( Environment.isExternalStorageManager())
                {
                    setResult(RESULT_OK)
                    finish()
                }
            }
            else if(hasStoragePermission())
            {
                setResult(RESULT_OK)
                finish()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION) {
            if(hasStoragePermission())
            {
                setResult(RESULT_OK)
                finish()
            }
            else
            {
                deniedOnce = true
                tv_allow?.text = "Open Settings"
                tv_open_settings?.doVisible()
            }
        }
    }
}