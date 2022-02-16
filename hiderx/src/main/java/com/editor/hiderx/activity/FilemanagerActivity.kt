package com.editor.hiderx.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.editor.hiderx.*
import com.editor.hiderx.dataclass.FileDataClass
import com.editor.hiderx.fragments.*
import com.editor.hiderx.listeners.ActivityFragmentListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rocks.addownplayer.PlayerActivity
import com.rocks.addownplayer.PlayerUtils
import java.io.File

const val REQUEST_CODE_FOR_SHARE : Int= 123

class FilemanagerActivity : AppCompatActivity(),ActivityFragmentListener {

    private var uploadPhotosFragment : UploadPhotosFragment? = null
    private var uploadAudiosFragment : UploadAudiosFragment? = null
    private var uploadVideosFragment : UploadVideosFragment? = null
    var backPressed: Boolean = false
    private var mIntent: Intent? = null
    private var fromHomeScreen: Boolean = false
    var hiddenFilesFragment : HiddenFilesFragment? = null
    var uploadFilesFragment : UploadFilesFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_layout)
        fromHomeScreen = intent.getBooleanExtra(Utility.KEY_FROM_HOME_SCREEN, false)
        if(fromHomeScreen)
        {
            loadUploadFilesFragment("")
        }
        else
        {
            loadHiddenFilesFragment()
        }
    }

    private fun loadUploadFilesFragment(path : String) {
        FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_FILE_CLICK","FROM_FILE_MANAGER")
        uploadFilesFragment = UploadFilesFragment.getInstance(path)
        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container,
            uploadFilesFragment!!
        ).addToBackStack(null).commit()
    }

    private fun loadHiddenFilesFragment() {
        hiddenFilesFragment = HiddenFilesFragment()
        hiddenFilesFragment?.onUploadClickListener = this
        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container,
            hiddenFilesFragment!!
        ).commitAllowingStateLoss()
    }

    fun viewFile(listOfFiles: ArrayList<FileDataClass>, position: Int)
    {
        try {
            if(listOfFiles[position].mimeType?.startsWith("image") == true)
            {
                loadImageViewerFragment(listOfFiles,position)
            }
            else if(listOfFiles[position].mimeType?.startsWith("video") == true)
            {
                backPressed = true
                VideoDataHolder.filesData = listOfFiles
                VideoDataHolder.data = null
                val mIntent = Intent(this,ExoPlayerMainActivity::class.java)
                mIntent.putExtra("pos",position)
                startActivityForResult(mIntent,REQUEST_CODE_PLAY_VIDEO)
            }
            else if(listOfFiles[position].mimeType?.startsWith("audio") == true)
            {
                backPressed = true
                try {
                    val arrayList : ArrayList<String> = ArrayList()
                    for(i in listOfFiles)
                    {
                        arrayList.add(i.path)
                    }
                    val intent = Intent(this, PlayerActivity::class.java)
                    intent.putExtra(PlayerUtils.LIST_EXTRA,arrayList)
                    intent.putExtra(PlayerUtils.POSITION_EXTRA,position)
                    intent.putExtra(PlayerUtils.APP_NAME , PlayerUtils.RADIO_FM_APP)
                    startActivity(intent)
                    /*val intent = Intent(Intent.ACTION_VIEW)
                    val  uri= FileProvider.getUriForFile(this,"$APPLICATION_ID.provider", File(hiddenFiles.path))
                    if(uri!=null)
                    {
                        intent.setDataAndType(uri,hiddenFiles.type)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }
                    else
                    {
                        Toast.makeText(this,"No Apps found to open such a file", Toast.LENGTH_LONG).show()
                    }*/
                }
                catch (e : java.lang.Exception)
                {
                    Toast.makeText(this,"No Apps found to open this file", Toast.LENGTH_LONG).show()
                }
            }
            else
            {
                backPressed = true
                val intent = Intent(Intent.ACTION_VIEW)
                val  uri= FileProvider.getUriForFile(
                    this,
                    "$APPLICATION_ID.provider",
                    File(listOfFiles[position].path)
                )
                if(uri!=null)
                {
                    intent.setDataAndType(uri, listOfFiles[position].mimeType!!)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
                else
                {
                    Toast.makeText(this, "No Apps found to open such a file", Toast.LENGTH_LONG).show()
                }
            }
        }
        catch (e: Exception)
        {
            Toast.makeText(this, "No Apps found to open this file", Toast.LENGTH_LONG).show()
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun loadImageViewerFragment(hiddenFiles : ArrayList<FileDataClass>,position:Int) {
        FirebaseAnalyticsUtils.sendEvent(this,"PHOTO_VIEWED","FROM_PHOTO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, PhotoViewer.newInstance(
            null,
            position,
            hiddenFiles)).addToBackStack(null).commit()
    }

    override fun onPause() {
        super.onPause()
        if(!backPressed)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR, true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


    override fun onResume() {
        super.onResume()
        backPressed = false
        if(mIntent!=null)
        {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
    }

    override fun onUploadClick(path : String) {
        when
        {
            path.startsWith(StorageUtils.getPhotosHiderDirectory()) -> loadUploadPhotosFragment(path)
            path.startsWith(StorageUtils.getAudiosHiderDirectory()) -> loadUploadAudiosFragment(path)
            path.startsWith(StorageUtils.getVideosHiderDirectory()) -> loadUploadVideosFragment(path)
            path.startsWith(StorageUtils.getOthersHiderDirectory()) -> loadUploadFilesFragment(path)
            path.startsWith(StorageUtils.getDocumentsHiderDirectory()) -> loadUploadFilesFragment(path)
        }
        //loadUploadFilesFragment()
    }

    private fun loadUploadPhotosFragment(path: String) {
        uploadPhotosFragment = UploadPhotosFragment.getInstance(path)
        supportFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                uploadPhotosFragment!!
        ).addToBackStack(null).commit()
    }

    private fun loadUploadAudiosFragment(path: String) {
        uploadAudiosFragment = UploadAudiosFragment.getInstance(path)
        supportFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                uploadAudiosFragment!!
        ).addToBackStack(null).commit()
    }

    private fun loadUploadVideosFragment(path: String) {
        uploadVideosFragment = UploadVideosFragment.getInstance(path)
        supportFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                uploadVideosFragment!!
        ).addToBackStack(null).commit()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_PLAY_VIDEO)
        {
            backPressed = true
            hiddenFilesFragment?.refreshData()
        }
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
       /* if(fragment is UploadFilesFragment)
        {
            if(fromHomeScreen)
            {
                finish()
            }
            else
            {
                if(model == null)
                    model =  ViewModelProvider(this).get(DataViewModel::class.java)
                model?.getFilemanagerData(false)
                backPressed = true
                super.onBackPressed()
            }
        }
        else*/
        if(fragment is HiddenFilesFragment && !hiddenFilesFragment?.doExit!!)
        {
            fragment.onPressedBack()
        }
        else if(fragment is UploadFilesFragment)
        {
            if(fragment.selectedFiles.isEmpty())
            {
                if(uploadFilesFragment?.doExit!!)
                {
                    if(fromHomeScreen)
                    {
                        finish()
                    }
                    else
                    {
                        uploadFilesFragment?.clearData()
                        hiddenFilesFragment?.currentPath = fragment.folderToHide
                        hiddenFilesFragment?.refreshData()
                        fragment.cancelActionMode()
                        super.onBackPressed()
                    }
                }
                else
                {
                    fragment.onPressedBack()
                }
            }
            else
            {
                fragment.cancelActionMode()
            }
        }
        else if(fragment is UploadPhotosFragment)
        {
            if(fragment.selectedImages.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else
                {
                    hiddenFilesFragment?.currentPath = uploadPhotosFragment?.xhiderDirectory
                    hiddenFilesFragment?.refreshData()
                    uploadPhotosFragment?.cancelActionMode()
                    super.onBackPressed()
                }
            }
            else
            {
                uploadPhotosFragment?.cancelActionMode()
            }
        }
        else if(fragment is UploadAudiosFragment)
        {
            if(fragment.selectedAudios.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else
                {
                    hiddenFilesFragment?.currentPath = uploadAudiosFragment?.xhiderDirectory
                    hiddenFilesFragment?.refreshData()
                    uploadAudiosFragment?.cancelActionMode()
                    super.onBackPressed()
                }
            }
            else
            {
                uploadAudiosFragment?.cancelActionMode()
            }
        }
        else if(fragment is UploadVideosFragment)
        {
            if(fragment.selectedVideos.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else
                {
                    hiddenFilesFragment?.currentPath = uploadVideosFragment?.xhiderDirectory
                    hiddenFilesFragment?.refreshData()
                    uploadVideosFragment?.cancelActionMode()
                    super.onBackPressed()
                }
            }
            else
            {
                uploadVideosFragment?.cancelActionMode()
            }
        }
        else if(fragment is PhotoViewer)
        {
            backPressed = true
            super.onBackPressed()
            hiddenFilesFragment?.refreshData()
        }
        else
        {
            backPressed = true
            super.onBackPressed()
        }
    }

}