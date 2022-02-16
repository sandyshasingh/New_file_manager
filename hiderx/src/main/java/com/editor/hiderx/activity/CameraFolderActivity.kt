package com.editor.hiderx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.editor.hiderx.*
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.fragments.*
import com.editor.hiderx.listeners.onUploadClickListenerForCamera
import kotlinx.android.synthetic.main.fragment_camera_folder.*

class CameraFolderActivity : AppCompatActivity(), onUploadClickListenerForCamera {

    private var mIntent: Intent? = null
    var backPressed: Boolean = false
    var cameraFolderFragment: CameraFolderFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_layout)
        loadCameraFolderFragment()
    }

    private fun loadCameraFolderFragment() {
        cameraFolderFragment = CameraFolderFragment()
        cameraFolderFragment?.onUploadClickListenerForCamera = this
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, cameraFolderFragment!!).commitAllowingStateLoss()
    }

    override fun onPause() {
        super.onPause()
        if (!backPressed) {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR, true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onResume() {
        super.onResume()
        backPressed = false
        if (mIntent != null) {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
    }

    override fun onUploadPhotoClicked(path: String) {
        loadUploadPhotosFragment(path)
    }

    private fun loadUploadPhotosFragment(path: String) {
        FirebaseAnalyticsUtils.sendEvent(this, "UPLOAD_PHOTO_CLICK", "FROM_CAMERA_SCREEN")
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, UploadPhotosFragment.getInstance(path))
            .addToBackStack(null).commit()
    }

    override fun onUploadVideoClicked(path: String) {
        loadUploadVideoFragment(path)
    }

    private fun loadPhotoViewerFragment(hiddenFiles: List<HiddenFiles>, position: Int) {
        FirebaseAnalyticsUtils.sendEvent(this, "PHOTO_VIEWED", "FROM_PHOTO_SCREEN")
        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container, PhotoViewer.newInstance(
                hiddenFiles as ArrayList<HiddenFiles>,
                position,
                null
            )
        ).addToBackStack(null).commit()
    }

    override fun onFileClicked(hiddenFiles: List<HiddenFiles>, position: Int) {
        if (hiddenFiles[position].type?.startsWith("image") == true)
            loadPhotoViewerFragment(hiddenFiles, position)
        else if (hiddenFiles[position].type?.startsWith("video") == true) {
            backPressed = true
            VideoDataHolder.data = hiddenFiles
            VideoDataHolder.filesData = null
            val mIntent = Intent(this,ExoPlayerMainActivity::class.java)
            mIntent.putExtra("pos",position)
            startActivityForResult(mIntent,REQUEST_CODE_PLAY_VIDEO)
        }
        /* try {
             backPressed = true
             val intent = Intent(Intent.ACTION_VIEW)
             val  uri= FileProvider.getUriForFile(
                     this,
                     "$APPLICATION_ID.provider",
                     File(hiddenFiles.path)
             )
             if(uri!=null)
             {
                 intent.setDataAndType(uri, hiddenFiles.type)
                 intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                 startActivity(intent)
             }
             else
             {
                 Toast.makeText(this, "No Apps found to open such a file", Toast.LENGTH_LONG).show()
             }
         }
         catch (e: Exception)
         {
             Toast.makeText(this, "No Apps found to open this file", Toast.LENGTH_LONG).show()
             FirebaseCrashlytics.getInstance().log(e.toString())
             FirebaseCrashlytics.getInstance().recordException(e)
         }*/

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_PLAY_VIDEO)
        {
            backPressed = true
            val currentPosition = cameraFolderFragment?.view_pager?.currentItem!!
            val placeHolderFragment =
                cameraFolderFragment?.view_pager?.adapter?.instantiateItem(
                    cameraFolderFragment?.view_pager!!,
                    currentPosition
                ) as PlaceholderFragment
            placeHolderFragment.refreshData(placeHolderFragment.currentPath)
        }
    }

    private fun loadUploadVideoFragment(path: String) {
        FirebaseAnalyticsUtils.sendEvent(this, "UPLOAD_VIDEO_CLICK", "FROM_CAMERA_SCREEN")
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, UploadVideosFragment.getInstance(path))
            .addToBackStack(null).commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is UploadPhotosFragment -> {
                if (currentFragment.selectedImages.isEmpty()) {
                    val currentPosition = cameraFolderFragment?.view_pager?.currentItem!!
                    val placeHolderFragment =
                        cameraFolderFragment?.view_pager?.adapter?.instantiateItem(
                            cameraFolderFragment?.view_pager!!,
                            currentPosition
                        ) as PlaceholderFragment
                    placeHolderFragment.refreshData(currentFragment.xhiderDirectory)
                    super.onBackPressed()
                } else {
                    currentFragment.cancelActionMode()
                }
            }
            is UploadVideosFragment -> {
                if (currentFragment.selectedVideos.isEmpty()) {
                    val currentPosition = cameraFolderFragment?.view_pager?.currentItem!!
                    val placeHolderFragment =
                        cameraFolderFragment?.view_pager?.adapter?.instantiateItem(
                            cameraFolderFragment?.view_pager!!,
                            currentPosition
                        ) as PlaceholderFragment
                    placeHolderFragment.refreshData(currentFragment.xhiderDirectory)
                    super.onBackPressed()
                } else {
                    currentFragment.cancelActionMode()
                }
            }
            is CameraFolderFragment -> {
                val currentPosition = currentFragment.view_pager.currentItem
                val placeHolderFragment = currentFragment.view_pager.adapter?.instantiateItem(
                    currentFragment.view_pager,
                    currentPosition
                ) as PlaceholderFragment
                if (placeHolderFragment.isActionMode) {
                    when (currentPosition) {
                        0 -> placeHolderFragment.cancelActionModeForPhotos()
                        1 -> placeHolderFragment.cancelActionModeForVideos()
                        2 -> placeHolderFragment.cancelActionModeForFolders()
                    }
                } else {
                    backPressed = true
                    super.onBackPressed()
                }
            }
            is PhotoViewer -> {
                backPressed = true
                super.onBackPressed()
                    val currentPosition = cameraFolderFragment?.view_pager?.currentItem!!
                    val placeHolderFragment =
                        cameraFolderFragment?.view_pager?.adapter?.instantiateItem(
                            cameraFolderFragment?.view_pager!!,
                            currentPosition
                        ) as PlaceholderFragment
                    placeHolderFragment.refreshData(placeHolderFragment.currentPath)
            }
            else -> {
                backPressed = true
                super.onBackPressed()
            }
        }
    }
}