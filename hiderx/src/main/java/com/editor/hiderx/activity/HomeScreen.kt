package com.editor.hiderx.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.editor.hiderx.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.Utils
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.header_default_device.*
import kotlinx.android.synthetic.main.layout_home_screen.*

const val MORE_APPS_LINK = "https://play.google.com/store/apps/developer?id=ASD+Dev+Video+Player+for+All+Format"
class HomeScreen : AppCompatActivity() {


    private var deniedCamera: Boolean = false
    private var homePressed: Boolean = true
    private var mIntent: Intent? = null
    private var hasCameraPermission: Boolean = false
    private val camera_permissions = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        val fromSetUpPassword = intent?.getBooleanExtra(HiderUtils.KEY_FROM_SETUP_PASSWORD, false)
        nav_view?.layoutParams?.width = (Resources.getSystem().displayMetrics.widthPixels * 0.5).toInt()
        nav_view?.requestLayout()
        tv_profile?.text = HiderUtils.getSharedPreference(this, HiderUtils.KEY_FOR_USER_NAME, "Unknown")
        setListeners()
        if(HiderUtils.getLongSharedPreference(this, HiderUtils.Last_File_Viewed_Time)<HiderUtils.getLongSharedPreference(this, HiderUtils.Last_File_Insert_Time))
            img_new_tag?.doVisible()
        if(fromSetUpPassword!=null && fromSetUpPassword)
            HiderUtils.showHelpDialog(this)
    }

    override fun onPause() {
        super.onPause()
        if(homePressed)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR, true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onResume() {
        super.onResume()
        homePressed = true
        if(mIntent!=null)
        {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
        else
        {
            if(HiderUtils.getLongSharedPreference(this, HiderUtils.Last_File_Viewed_Time)<HiderUtils.getLongSharedPreference(this, HiderUtils.Last_File_Insert_Time))
                img_new_tag?.doVisible()
            else
            img_new_tag?.doGone()
        }
    }

    private fun setListeners() {
        category_photos?.setOnClickListener()
        {
                homePressed = false
                startActivity(Intent(this, PhotosActivity::class.java))
                FirebaseAnalyticsUtils.sendEvent(this,"PHOTOS_CATEGORY_CLICK","PHOTOS_CATEGORY_CLICK")
        }

        category_videos?.setOnClickListener()
        {
            homePressed = false
            startActivity(Intent(this, VideosActivity::class.java))
            FirebaseAnalyticsUtils.sendEvent(this,"VIDEOS_CATEGORY_CLICK","VIDEOS_CATEGORY_CLICK")
        }

        rl_audio?.setOnClickListener()
        {
            homePressed = false
            startActivity(Intent(this, AudiosActivity::class.java))
            FirebaseAnalyticsUtils.sendEvent(this,"AUDIOS_CATEGORY_CLICK","AUDIOS_CATEGORY_CLICK")
        }

        category_file_manager?.setOnClickListener()
        {
            HiderUtils.setLongSharedPreference(this, HiderUtils.Last_File_Viewed_Time, System.currentTimeMillis())
            homePressed = false
                startActivity(Intent(this, FilemanagerActivity::class.java))
            FirebaseAnalyticsUtils.sendEvent(this,"FILES_CATEGORY_CLICK","FILES_CATEGORY_CLICK")
        }

        category_camera?.setOnClickListener()
        {
            homePressed = false
            hasCameraPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if(hasCameraPermission)
            {

                    startActivity(Intent(this, CameraActivity::class.java))
            }
            else
            {
                if(deniedCamera)
                {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.parse("$SCHEME:$packageName")
                    intent.data = uri
                    startActivityForResult(
                            intent, REQUEST_CODE_FOR_SETTINGS_ACTIVITY
                    )
                }
                else
                {
                    ActivityCompat.requestPermissions(this, camera_permissions, CAMERA_PERMISSION)
                }
            }
            FirebaseAnalyticsUtils.sendEvent(this,"CAMERA_CATEGORY_CLICK","CAMERA_CATEGORY_CLICK")
        }

        layout_upload_video?.setOnClickListener()
        {
            homePressed = false
            startVideoListActivity()
            FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_VIDEO_CLICK","FROM_HOME_SCREEN")
        }

        layout_upload_photo?.setOnClickListener()
        {
            homePressed = false
            startPhotosListActivity()
            FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_PHOTO_CLICK","FROM_HOME_SCREEN")
        }

        layout_upload_audio?.setOnClickListener()
        {
            homePressed = false
            startAudioListActivity()
            FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_AUDIO_CLICK","FROM_HOME_SCREEN")
        }

        layout_upload_files?.setOnClickListener()
        {
            homePressed = false
            startFileManagerActivity()
            FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_FILE_CLICK","FROM_HOME_SCREEN")
        }

        img_toggle?.setOnClickListener()
        {
            // FirebaseAnalyticsUtils.sendEvent(this, "hamberger_open", "hamberger_open")
            if(drawer_layout?.isDrawerOpen(nav_view)!!)
            {
                drawer_layout?.closeDrawer(GravityCompat.START)
            }
            else
            {
                drawer_layout?.openDrawer(GravityCompat.START)
            }
        }

        edit_password?.setOnClickListener()
        {
            homePressed = false
            val intent = Intent(this, CalculatorActivity::class.java)
                intent.putExtra(Utility.KEY_CHANGE_PASSWORD, true)
                startActivity(intent)
            FirebaseAnalyticsUtils.sendEvent(this,"EDIT_PASSWORD","FROM_HOME_SCREEN")
            finish()
        }

        more_apps?.setOnClickListener()
        {
            try {
                homePressed = false
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(MORE_APPS_LINK)
                startActivity(i)
                FirebaseAnalyticsUtils.sendEvent(this,"MORE_APPS","MORE_APPS")
            } catch (e: Exception) {
                Toast.makeText(this, " This option can not open in your device",Toast.LENGTH_LONG).show()
                FirebaseCrashlytics.getInstance().log(e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        setting?.setOnClickListener()
        {
            homePressed = false
            startSettingsActivity()
            FirebaseAnalyticsUtils.sendEvent(this,"SETTINGS_ACTIVITY","SETTINGS_ACTIVITY")
        }

        edit_image?.setOnClickListener()
        {
            val inflater: LayoutInflater = LayoutInflater.from(this)
            val view1: View = inflater.inflate(R.layout.edit_profile_dialog, null)
            val name = view1.findViewById<EditText>(R.id.editText)
            name.setText(
                    HiderUtils.getSharedPreference(this, HiderUtils.KEY_FOR_USER_NAME, getString(R.string.username))
            )
            val dialog = AlertDialog.Builder(this)
            dialog.setView(view1)
            dialog.setNegativeButton(
                    R.string.butn_remove, DialogInterface.OnClickListener()
            { dialog, which ->

            })
            dialog.setPositiveButton(
                    R.string.butn_save, DialogInterface.OnClickListener()
            { dialog, which ->
                tv_profile?.text = name.text
                HiderUtils.setSharedPreference(this, HiderUtils.KEY_FOR_USER_NAME, name?.text?.toString())
            })
            dialog.setNeutralButton(
                    R.string.butn_close, null
            )
            dialog.show()
            FirebaseAnalyticsUtils.sendEvent(this,"EDIT_PROFILE","EDIT_PROFILE")
        }
    }

    private fun startSettingsActivity() {
        startActivity(Intent(this,SettingsActivity::class.java))
    }

    private fun startVideoListActivity() {
        val intent = Intent(this, VideosActivity::class.java)
        intent.putExtra(Utility.KEY_FROM_HOME_SCREEN, true)
        startActivity(intent)
    }

    private fun startAudioListActivity() {
        val intent = Intent(this, AudiosActivity::class.java)
        intent.putExtra(Utility.KEY_FROM_HOME_SCREEN, true)
        startActivity(intent)
    }

    private fun startFileManagerActivity() {
        val intent = Intent(this, FilemanagerActivity::class.java)
        intent.putExtra(Utility.KEY_FROM_HOME_SCREEN, true)
        startActivity(intent)
    }

    private fun startPhotosListActivity() {
        val intent = Intent(this, PhotosActivity::class.java)
        intent.putExtra(Utility.KEY_FROM_HOME_SCREEN, true)
        startActivity(intent)
    }

    override fun onBackPressed() {
        homePressed = false
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION)
        {
            hasCameraPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if(hasCameraPermission)
            {
                homePressed = false
                mIntent = null
                startActivity(Intent(this,CameraActivity::class.java))
            }
            else
            {
                deniedCamera = true
                Toast.makeText(this,"Permission required",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_FOR_SETTINGS_ACTIVITY)
        {
            hasCameraPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if(hasCameraPermission)
            {
                homePressed = false
                mIntent = null
                startActivity(Intent(this,CameraActivity::class.java))
            }
        }
    }
}