package com.editor.hiderx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.editor.hiderx.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.fragments.*
import com.editor.hiderx.listeners.ActivityFragmentListener
import com.rocks.addownplayer.PlayerActivity
import com.rocks.addownplayer.PlayerUtils
import java.lang.Exception

class AudiosActivity : AppCompatActivity(), ActivityFragmentListener {

    var hiddenAudiosFragment : HiddenAudiosFragment? = null
    var pressedBack: Boolean = false
    private var mIntent: Intent? = null
    private var fromHomeScreen: Boolean = false
    private var model: DataViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_layout)
        fromHomeScreen = intent.getBooleanExtra(Utility.KEY_FROM_HOME_SCREEN,false)
        if(fromHomeScreen)
        {
            loadUploadAudiosFragment(StorageUtils.getAudiosHiderDirectory())
        }
        else
        {
            loadHiddenAudiosFragment()
        }
    }

    fun viewFile(hiddenFiles: List<HiddenFiles>, adapterPosition: Int)
    {
        pressedBack = true
        try {
            val arrayList : ArrayList<String> = ArrayList()
            for(i in hiddenFiles)
            {
                arrayList.add(i.path)
            }
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerUtils.LIST_EXTRA,arrayList)
            intent.putExtra(PlayerUtils.POSITION_EXTRA,adapterPosition)
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
        catch (e : Exception)
        {
            Toast.makeText(this,"No Apps found to open this file", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if(!pressedBack)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR,true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


    override fun onResume() {
        super.onResume()
        pressedBack = false
        if(mIntent!=null)
        {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
    }

    private fun loadHiddenAudiosFragment() {
        hiddenAudiosFragment = HiddenAudiosFragment()
        hiddenAudiosFragment?.onUploadClickListener = this
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, hiddenAudiosFragment!!).commitAllowingStateLoss()
    }

    override fun onUploadClick(path : String) {
        loadUploadAudiosFragment(path)
    }

    private fun loadUploadAudiosFragment(folderToHide: String) {
        FirebaseAnalyticsUtils.sendEvent(this,"UPLOAD_AUDIO_CLICK","FROM_AUDIO_SCREEN")
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, UploadAudiosFragment.getInstance(folderToHide)).addToBackStack(null).commit()
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment is UploadAudiosFragment)
        {
            if(fragment.selectedAudios.isEmpty())
            {
                if(fromHomeScreen)
                {
                    finish()
                }
                else {
                    pressedBack = true
                    hiddenAudiosFragment?.currentPath = fragment.xhiderDirectory
                    hiddenAudiosFragment?.refreshData()
                    super.onBackPressed()
                }
            }
            else
            {
                fragment.cancelActionMode()
            }
        }
        else if(fragment is HiddenAudiosFragment)
        {
            if(fragment.doExit && fragment.selectedAudios.isEmpty())
                super.onBackPressed()
            else
                fragment.onPressedBack()
        }
    }
}