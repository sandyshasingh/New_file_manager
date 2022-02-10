package com.simplemobiletools.filemanager.pro

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getSDCardPath
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.filemanager.pro.activities.FileManagerMainActivity

class SplashScreen : BaseSimpleActivity(),GroupVideoPhotoAsyncTask.RecentFetchAsyncCompleteListener {

    private val SPLASH_DISPLAY_LENGTH = 30L
  //  var viewModel : DataViewModel? = null


//    var recent_files : MutableLiveData<Map<String, List<ListItem>>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        getSDCardPath()

        //   viewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        handlePermission(PERMISSION_WRITE_STORAGE) {


            if(it){
                val intent = Intent(this, ServiceIntent::class.java).apply {}
                startService(intent)

                GroupVideoPhotoAsyncTask(this,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            }
            else
                startFileManager()
        }

      //  viewModel?.fetchRecent(this)



    }

    override fun recentFetchCompleted(audiosList: RecentUpdatedFiles) {
     //   AppDataHolder.mfinalKeys = audiosList.mKeys
        AppDataHolder.mfinalValues = audiosList
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