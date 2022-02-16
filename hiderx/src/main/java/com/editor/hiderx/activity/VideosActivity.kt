package com.editor.hiderx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.editor.hiderx.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.fragments.HiddenVideosFragment
import com.editor.hiderx.fragments.UploadVideosFragment
import com.editor.hiderx.fragments.VideoViewer
import com.editor.hiderx.listeners.ActivityFragmentListener

const val REQUEST_CODE_PLAY_VIDEO = 7654

class VideosActivity : AppCompatActivity(), ActivityFragmentListener {

    var hiddenVideosFragment  : HiddenVideosFragment? = null
    private var model: DataViewModel? = null
    private var fromHomeScreen : Boolean = false
    var mIntent : Intent? = null
    var backPressed : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_layout)
        fromHomeScreen = intent.getBooleanExtra(Utility.KEY_FROM_HOME_SCREEN,false)
        if(fromHomeScreen)
        {
            loadUploadVideosFragment(StorageUtils.getVideosHiderDirectory())
        }
        else
        {
            loadHiddenVideosFragment()
        }
    }

    private fun loadHiddenVideosFragment() {
        hiddenVideosFragment = HiddenVideosFragment()
        hiddenVideosFragment?.activityFragmentListener = this
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, hiddenVideosFragment!!).commitAllowingStateLoss()
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

    fun viewFile(hiddenVideos: List<HiddenFiles>, position: Int)
    {
        backPressed = true
      //  loadVideoViewerFragment(hiddenVideos,position)
        VideoDataHolder.data = hiddenVideos
        VideoDataHolder.filesData = null
        val mIntent = Intent(this,ExoPlayerMainActivity::class.java)
        mIntent.putExtra("pos",position)
        startActivityForResult(mIntent,REQUEST_CODE_PLAY_VIDEO)
        //HiderUtils.playVideo(this,hiddenVideos.path,hiddenVideos.type!!)
    }

    private fun loadVideoViewerFragment(hiddenVideos: List<HiddenFiles>, position: Int) {
        FirebaseAnalyticsUtils.sendEvent(this,"VIDEO_VIEWED","FROM_VIDEO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, VideoViewer.newInstance(
            hiddenVideos as ArrayList<HiddenFiles>,
            position,
            null
        )).addToBackStack(null).commit()
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
        loadUploadVideosFragment(path)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_PLAY_VIDEO)
        {
            hiddenVideosFragment?.refreshData()
        }
    }


    private fun loadUploadVideosFragment(path: String) {
        FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_VIDEO_CLICK","FROM_VIDEO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container,UploadVideosFragment.getInstance(path)).addToBackStack(null).commit()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment is UploadVideosFragment)
        {
            if(fragment.selectedVideos.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else {
                    backPressed = true
                    hiddenVideosFragment?.currentPath = fragment.xhiderDirectory
                    hiddenVideosFragment?.refreshData()
                    super.onBackPressed()
                }
            }
            else
            {
                fragment.cancelActionMode()
            }
        }
        else if(fragment is HiddenVideosFragment)
        {
            if(fragment.doExit && fragment.selectedVideos.isEmpty())
                super.onBackPressed()
            else
                fragment.onPressedBack()
        }
        else if(fragment is VideoViewer)
        {
            hiddenVideosFragment?.refreshData()
            super.onBackPressed()
        }
    }
}