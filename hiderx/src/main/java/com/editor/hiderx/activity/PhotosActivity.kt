package com.editor.hiderx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.editor.hiderx.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.fragments.HiddenPhotosFragment
import com.editor.hiderx.fragments.PhotoViewer
import com.editor.hiderx.fragments.UploadPhotosFragment
import com.editor.hiderx.listeners.ActivityFragmentListener
import kotlinx.android.synthetic.main.fragment_camera_folder.*

class PhotosActivity : AppCompatActivity(), ActivityFragmentListener {


    var hiddenPhotosFragment : HiddenPhotosFragment? = null
    var backPressed: Boolean = false
    private var mIntent: Intent? = null
    private var fromHomeScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_layout)
        fromHomeScreen = intent.getBooleanExtra(Utility.KEY_FROM_HOME_SCREEN,false)
        if(fromHomeScreen)
        {
            loadUploadPhotosFragment(StorageUtils.getPhotosHiderDirectory())
        }
        else
        {
            loadHiddenPhotosFragment()
        }
    }

    override fun onPause() {
        super.onPause()
        if(!backPressed)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR,true)
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

    private fun loadHiddenPhotosFragment() {
        hiddenPhotosFragment = HiddenPhotosFragment()
        hiddenPhotosFragment?.onUploadClickListener = this
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, hiddenPhotosFragment!!).commitAllowingStateLoss()
    }

    fun viewFile(hiddenFiles : List<HiddenFiles>,position : Int)
    {
        loadPhotoViewerFragment(hiddenFiles,position)
        /*backPressed = true
        try {
            val intent = Intent(Intent.ACTION_VIEW)
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
            }
        }
        catch (e : Exception)
        {
            Toast.makeText(this,"No Apps found to open this file", Toast.LENGTH_LONG).show()
        }*/
    }

    private fun loadPhotoViewerFragment(hiddenFiles: List<HiddenFiles>, position: Int) {
        FirebaseAnalyticsUtils.sendEvent(this,"PHOTO_VIEWED","FROM_PHOTO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, PhotoViewer.newInstance(
            hiddenFiles as ArrayList<HiddenFiles>,
            position,
            null
        )).addToBackStack(null).commit()
    }

    override fun onUploadClick(path : String) {
        loadUploadPhotosFragment(path)
    }

    private fun loadUploadPhotosFragment(path: String) {
        FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_PHOTO_CLICK","FROM_PHOTO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, UploadPhotosFragment.getInstance(path)).addToBackStack(null).commit()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment is UploadPhotosFragment)
        {
            if(fragment.selectedImages.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else {
                    backPressed = true
                    hiddenPhotosFragment?.currentPath = fragment.xhiderDirectory
                    hiddenPhotosFragment?.refreshData()
                    super.onBackPressed()
                }
            }
            else
            {
                fragment.cancelActionMode()
            }
        }
        else if(fragment is HiddenPhotosFragment)
        {
            if(fragment.doExit && fragment.selectedImages.isEmpty())
                super.onBackPressed()
            else
                fragment.onPressedBack()
        }
        else if(fragment is PhotoViewer)
        {
            hiddenPhotosFragment?.refreshData()
            super.onBackPressed()
        }
        else
            super.onBackPressed()
    }

}