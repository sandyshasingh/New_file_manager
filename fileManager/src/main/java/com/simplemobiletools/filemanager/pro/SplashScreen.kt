package com.simplemobiletools.filemanager.pro

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity
import com.simplemobiletools.filemanager.pro.helpers.DataViewModel
import com.simplemobiletools.filemanager.pro.models.ListItem

class SplashScreen : BaseSimpleActivity(),GroupVideoPhotoAsyncTask.RecentFetchAsyncCompleteListener {

    private val SPLASH_DISPLAY_LENGTH = 30L
  //  var viewModel : DataViewModel? = null
//    var recent_files : MutableLiveData<Map<String, List<ListItem>>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


     //   viewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if(it)
            GroupVideoPhotoAsyncTask(this,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            else
                startFileManager()
        }

      //  viewModel?.fetchRecent(this)



    }

    override fun recentFetchCompleted(audiosList: Map<String, List<ListItem>>?) {
        AppDataHolder.finalDataList = audiosList
        startFileManager()
         }

    private fun startFileManager() {
        Handler().postDelayed(Runnable {
            /* Create an Intent that will start the Menu-Activity. */

//            recent_files.value = audiosList
            val mainIntent = Intent(this, FileManagerMainActivity::class.java)
            this.startActivity(mainIntent)
            this.finish()


        }, SPLASH_DISPLAY_LENGTH)
    }
}