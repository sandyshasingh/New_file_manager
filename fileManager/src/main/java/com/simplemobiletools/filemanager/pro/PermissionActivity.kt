package com.simplemobiletools.filemanager.pro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import kotlinx.android.synthetic.main.activity_permission.*

class PermissionActivity : AppCompatActivity() {

    var deniedOnce : Boolean = false
    val permissions =arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private  val STORAGE_PERMISSION = 23
    private  val REQUEST_CODE_FOR_SETTINGS_ACTIVITY = 123
    private  val REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE = 125
    private  val SCHEME = "package"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        allow_tag.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                if(!Environment.isExternalStorageManager())
                {
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:$packageName")
                    ), REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE)
                }
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION)
            }
//
//            allow_tag.setOnClickListener()
//            {
//                if(deniedOnce)
//                {
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri: Uri = Uri.fromParts(SCHEME, packageName, null)
//                    intent.data = uri
//                    startActivityForResult(
//                        intent, REQUEST_CODE_FOR_SETTINGS_ACTIVITY
//                    )
//                }
//                else
//                {
//                    ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION)
//                }
//            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION) {
            val checkPermission= ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )== PackageManager.PERMISSION_GRANTED
          /*  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                checkPermission = ContextCompat.checkSelfPermission(  applicationContext,Environment.isExternalStorageManager()== PackageManager.PERMISSION_GRANTED)
            }*/
            if(checkPermission) {
                setResult(RESULT_OK)
                finish()
//                val intent = Intent(this,FileManagerMainActivity::class.java)
//                startActivity(intent)
            }
            else
            {
                deniedOnce = true
               // tv_open_settings?.visibility = View.VISIBLE
                allow_tag?.text = "OPEN SETTINGS"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                val checkPermission =  Environment.isExternalStorageManager()
                if(checkPermission) {
                    setResult(RESULT_OK)
                    finish()
//                val intent = Intent(this,FileManagerMainActivity::class.java)
//                startActivity(intent)
                }
                else
                {
                    deniedOnce = true
                    // tv_open_settings?.visibility = View.VISIBLE
                    allow_tag?.text = "OPEN SETTINGS"
                }
            }

        }
    }
}
