package com.editor.hiderx.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.editor.hiderx.*
import com.editor.hiderx.Utility.FLASH_OFF
import com.editor.hiderx.Utility.FLASH_ON
import com.editor.hiderx.camera.CameraPreview
import com.editor.hiderx.camera.FocusCircleView
import com.editor.hiderx.camera.MyCameraImpl
import com.editor.hiderx.camera.MyPreview
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import kotlinx.android.synthetic.main.layout_camera.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable

const val CAMERA_PERMISSION = 34
const val AUDIO_PERMISSION = 45

class CameraActivity : AppCompatActivity(),CoroutineScope by MainScope() {

    private var deniedOnce: Boolean = false
    private var homePressed: Boolean = true
    private var lastPhotoVideoPath: String? = null
    private var mediaScanner : MediaScanner? = null
    private var mFadeHandler: Handler? =null
    private var mFocusCircleView: FocusCircleView? = null
    lateinit var mTimerHandler: Handler
    private val CAPTURE_ANIMATION_DURATION = 100L
    private var mPreview: MyPreview? = null
    private lateinit var mCameraImpl: MyCameraImpl
    private var mIsInPhotoMode = false
    private var mIsCameraAvailable = false
    private var mIsVideoCaptureIntent = false
    private var mIsHardwareShutterHandled = false
    private var mCurrVideoRecTimer = 0
    var mLastHandledOrientation = 0
    private var hasCameraPermission: Boolean = false
    private val camera_permissions = arrayOf(Manifest.permission.CAMERA)
    private var mIntent: Intent? = null
    private var hasRecordAudioPermission: Boolean = false
    private val record_audio_permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_camera)
        btn_back?.setOnClickListener()
        {
            onBackPressed()
        }
        initVariables()
        hasCameraPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if(hasCameraPermission)
        {
            initCamera()
        }
        else
        {
            ActivityCompat.requestPermissions(this, camera_permissions, CAMERA_PERMISSION)
        }
    }

    private fun initButtons() {
        toggle_camera.setOnClickListener { toggleCamera() }
        rl_folders?.setOnClickListener()
        {
            homePressed = false
            loadCameraFolderActivity()
        }
        //last_photo_video_preview.setOnClickListener { showLastMediaPreview() }
        toggle_flash.setOnClickListener { toggleFlash() }
        shutter.setOnClickListener { shutterPressed() }
        //settings.setOnClickListener { launchSettings() }
        toggle_photo_video.setOnClickListener { handleTogglePhotoVideo() }
        img_settings?.setOnClickListener { mPreview?.showChangeResolutionDialog() }
    }

    override fun onPause() {
        super.onPause()
        if(homePressed)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR,true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


    private fun loadCameraFolderActivity() {
        startActivity(Intent(this,CameraFolderActivity::class.java))
    }

    private fun handleTogglePhotoVideo() {
        hasRecordAudioPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if(hasRecordAudioPermission)
        {
            togglePhotoVideo()
        }
        else
        {
            homePressed = false
            if(deniedOnce)
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
            ActivityCompat.requestPermissions(this, record_audio_permissions, AUDIO_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_FOR_SETTINGS_ACTIVITY)
        {
            hasRecordAudioPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if(hasRecordAudioPermission)
            {
                togglePhotoVideo()
            }
            else
                deniedOnce = true
        }
    }

    private fun togglePhotoVideo() {
        if (!checkCameraAvailable()) {
            return
        }

        if (mIsVideoCaptureIntent) {
            mPreview?.tryInitVideoMode()
        }

        mPreview?.setFlashlightState(FLASH_OFF)
        hideTimer()
        mIsInPhotoMode = !mIsInPhotoMode
        HiderUtils.setBooleanSharedPreference(this,HiderUtils.Is_PHOTO_MODE,mIsInPhotoMode)
        showToggleCameraIfNeeded()
        checkButtons()
        toggleBottomButtons(false)
    }

    private fun checkButtons() {
        if (mIsInPhotoMode) {
            initPhotoMode()
        } else {
            tryInitVideoMode()
        }
    }

    private fun tryInitVideoMode() {
        if (mPreview?.initVideoMode() == true) {
            initVideoButtons()
        } else {
            if (!mIsVideoCaptureIntent) {
                Toast.makeText(this,R.string.video_mode_error,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initVideoButtons() {
        toggle_photo_video.setImageResource(R.drawable.ic_camera_vector)
        showToggleCameraIfNeeded()
        shutter.setImageResource(R.drawable.ic_video_rec)
        setupPreviewImage()
        mPreview?.checkFlashlight()
    }

    private fun setupPreviewImage() {
        if(lastPhotoVideoPath!=null && File(lastPhotoVideoPath!!).exists())
        {
            val uri : Uri? = Uri.fromFile(File(lastPhotoVideoPath!!))
                last_photo_video_preview?.loadUri(uri)
        }
        else
        {
            last_photo_video_preview?.loadUri(null)
        }
    }

    private fun initPhotoMode() {
            toggle_photo_video.setImageResource(R.drawable.ic_video_vector)
            shutter.setImageResource(R.drawable.ic_shutter_vector)
            mPreview?.initPhotoMode()
            setupPreviewImage()
    }

    private fun showToggleCameraIfNeeded() {
        toggle_camera?.doInvisibleIf(mCameraImpl.getCountOfCameras() ?: 1 <= 1)
    }

    private fun hideTimer() {
        video_rec_curr_timer.text = StorageUtils.timeConversionInMinSec(0L)
        video_rec_curr_timer.doInvisible()
        mCurrVideoRecTimer = 0
        mTimerHandler.removeCallbacksAndMessages(null)
    }

    private fun shutterPressed() {
        if (checkCameraAvailable()) {
            handleShutter()
        }
    }

    private fun handleShutter() {
        if (mIsInPhotoMode) {
            toggleBottomButtons(true)
            mPreview?.tryTakePicture()
            capture_black_screen.animate().alpha(0.8f).setDuration(CAPTURE_ANIMATION_DURATION).withEndAction {
                capture_black_screen.animate().alpha(0f).setDuration(CAPTURE_ANIMATION_DURATION).start()
            }.start()
        } else {
            mPreview?.toggleRecording()
        }
    }

    override fun onBackPressed() {
        homePressed = false
        super.onBackPressed()
        if(mPreview!=null)
        {
            if(mPreview?.isRecording()!!)
            {
                mPreview?.toggleRecording()
            }
        }
    }

    fun toggleBottomButtons(hide: Boolean) {
        runOnUiThread {
            val alpha = if (hide) 0f else 1f
            shutter.animate().alpha(alpha).start()
            toggle_camera.animate().alpha(alpha).start()
            toggle_flash.animate().alpha(alpha).start()

            shutter.isClickable = !hide
            toggle_camera.isClickable = !hide
            toggle_flash.isClickable = !hide
        }
    }

    private fun toggleFlash() {
        if (checkCameraAvailable()) {
            mPreview?.toggleFlashlight()
        }
    }

    private fun toggleCamera() {
        if (checkCameraAvailable()) {
            mPreview?.toggleFrontBackCamera()
        }
    }

    private fun checkCameraAvailable(): Boolean {
        if (!mIsCameraAvailable) {
            Toast.makeText(this, "camera unavailable", Toast.LENGTH_LONG).show()
        }
        return mIsCameraAvailable
    }

    private fun initCamera() {
        initButtons()
        mPreview = CameraPreview(this, camera_texture_view, mIsInPhotoMode)
        view_holder.addView(mPreview as ViewGroup)
        mPreview?.setIsImageCaptureIntent(false)

        val imageDrawable = if (HiderUtils.getIntSharedPreference(this,HiderUtils.LAST_USED_CAMERA_ID).toString() == mCameraImpl.getBackCameraId().toString()) R.drawable.ic_camera_front_vector else R.drawable.ic_camera_rear_vector
        toggle_camera.setImageResource(imageDrawable)

        mFocusCircleView = FocusCircleView(applicationContext)
        view_holder.addView(mFocusCircleView)

        mTimerHandler = Handler()
        mFadeHandler = Handler()
        setupPreviewImage()

        val initialFlashlightState = if (HiderUtils.getBooleanSharedPreference(this,HiderUtils.TURN_FLASH_OFF_AT_STARTUP)) FLASH_OFF else HiderUtils.getIntSharedPreference(this,HiderUtils.FLASH_LIGHT_STATE)
        mPreview?.setFlashlightState(initialFlashlightState)
        updateFlashlightState(initialFlashlightState)
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
                initCamera()
            }
        }
        else if(requestCode == AUDIO_PERMISSION)
        {
            hasRecordAudioPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if(hasRecordAudioPermission)
            {
                togglePhotoVideo()
            }
            else
            {
                deniedOnce = true
            }
        }
    }

    fun setupDialogStuff(view: View, dialog: AlertDialog, titleId: Int = 0, titleText: String = "", callback: (() -> Unit)? = null) {
        if (isDestroyed || isFinishing) {
            return
        }

        /*val adjustedPrimaryColor = getAdjustedPrimaryColor()
        if (view is ViewGroup)
            updateTextColors(view)
        else if (view is MyTextView) {
            view.setColors(baseConfig.textColor, adjustedPrimaryColor, baseConfig.backgroundColor)
        }

        var title: TextView? = null
        if (titleId != 0 || titleText.isNotEmpty()) {
            title = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
            title.dialog_title_textview.apply {
                if (titleText.isNotEmpty()) {
                    text = titleText
                } else {
                    setText(titleId)
                }
                setTextColor(baseConfig.textColor)
            }
        }*/

        dialog.apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
           // setCustomTitle(title)
            setCanceledOnTouchOutside(true)
            show()
            /*getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(adjustedPrimaryColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(adjustedPrimaryColor)
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(adjustedPrimaryColor)*/

           /* val bgDrawable = resources.getColoredDrawableWithColor(R.drawable.dialog_bg, baseConfig.backgroundColor)
            window?.setBackgroundDrawable(bgDrawable)*/
        }
        callback?.invoke()
    }

    private fun initVariables() {
        hasRecordAudioPermission = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        mIsInPhotoMode = checkIsInPhotoMode() || !hasRecordAudioPermission
        mIsCameraAvailable = false
        lastPhotoVideoPath = HiderUtils.getSharedPreference(this,HiderUtils.LAST_PHOTO_VIDEO_PATH)
        mIsVideoCaptureIntent = false
        mIsHardwareShutterHandled = false
        mCurrVideoRecTimer = 0
        mLastHandledOrientation = 0
        mCameraImpl = MyCameraImpl(applicationContext)

        if (HiderUtils.getBooleanSharedPreference(this,HiderUtils.ALWAYS_OPEN_BACK_CAMERA)) {
                HiderUtils.setIntSharedPreference(this,HiderUtils.LAST_USED_CAMERA_ID,mCameraImpl.getBackCameraId())
        }
    }

    private fun checkIsInPhotoMode(): Boolean {
        val pref = getSharedPreferences("com.editor.hiderx", Context.MODE_PRIVATE)
        return if (pref.contains(HiderUtils.Is_PHOTO_MODE)) {
            pref.getBoolean(HiderUtils.Is_PHOTO_MODE, true)
        } else true
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
            mPreview?.onResumed()
            resumeCameraItems()
            setupPreviewImage()
            mFocusCircleView?.setStrokeColor(R.color.btn_blue)
            toggleBottomButtons(false)
        }
    }

    private fun resumeCameraItems() {
        showToggleCameraIfNeeded()
        //hideNavigationBarIcons()

        if (!mIsInPhotoMode) {
            initVideoButtons()
        }
    }

    fun setFlashAvailable(available : Boolean) {
        if (available) {
            toggle_flash.doVisible()
        } else {
            toggle_flash.doInvisible()
            toggle_flash.setImageResource(R.drawable.ic_flash_off_vector)
            mPreview?.setFlashlightState(FLASH_OFF)
        }
    }

    fun updateCameraIcon(isUsingFrontCamera: Boolean) {
        toggle_camera.setImageResource(if (isUsingFrontCamera) R.drawable.ic_camera_rear_vector else R.drawable.ic_camera_front_vector)
    }

    fun setIsCameraAvailable(available: Boolean) {
        mIsCameraAvailable = available
    }
    fun updateFlashlightState(state: Int) {
        HiderUtils.setIntSharedPreference(this,"flashlightState",state)
        val flashDrawable = when (state) {
            FLASH_OFF -> R.drawable.ic_flash_off_vector
            FLASH_ON -> R.drawable.ic_flash_on_vector
            else -> R.drawable.ic_flash_auto_vector
        }
        toggle_flash.setImageResource(flashDrawable)
    }

    fun drawFocusCircle(x: Float, y: Float) = mFocusCircleView?.drawFocusCircle(x, y)


    fun setRecordingState(isRecording : Boolean) {
        runOnUiThread {
            if (isRecording) {
                shutter.setImageResource(R.drawable.ic_video_stop)
                toggle_camera.doInvisible()
                showTimer()
            } else {
                shutter.setImageResource(R.drawable.ic_video_rec)
                showToggleCameraIfNeeded()
                hideTimer()
            }
        }
    }

    private fun showTimer() {
        video_rec_curr_timer.doVisible()
        setupTimer()
    }

    private fun setupTimer() {
        runOnUiThread(object : Runnable {
            override fun run() {
                mCurrVideoRecTimer += 1000
                video_rec_curr_timer.text = StorageUtils.timeConversionInMinSec(mCurrVideoRecTimer.toLong())
                mTimerHandler.postDelayed(this, 1000L)
            }
        })
    }

    fun insertVideoInDb(hiddenVideos: HiddenFiles) {
        launch {
            withContext(Dispatchers.IO)
            {
                if(mediaScanner == null)
                    mediaScanner = MediaScanner(this@CameraActivity)
                mediaScanner?.scan(hiddenVideos.path)
                lastPhotoVideoPath = hiddenVideos.path
                HiderUtils.setSharedPreference(this@CameraActivity,HiderUtils.LAST_PHOTO_VIDEO_PATH,lastPhotoVideoPath)
                HiddenFilesDatabase.getInstance(this@CameraActivity).hiddenFilesDao.insertFile(hiddenVideos)
                HiderUtils.setLongSharedPreference(this@CameraActivity,HiderUtils.Last_File_Insert_Time,System.currentTimeMillis())
            }
            withContext(Dispatchers.Main)
            {
                setupPreviewImage()
            }
        }
    }

    fun insertPhotoInDb(hiddenFile : HiddenFiles) {
        launch {
            withContext(Dispatchers.IO)
            {
                if(mediaScanner == null)
                    mediaScanner = MediaScanner(this@CameraActivity)
                mediaScanner?.scan(hiddenFile.path)
                lastPhotoVideoPath = hiddenFile.path
                HiderUtils.setSharedPreference(this@CameraActivity,HiderUtils.LAST_PHOTO_VIDEO_PATH,lastPhotoVideoPath)
                HiddenFilesDatabase.getInstance(this@CameraActivity).hiddenFilesDao.insertFile(hiddenFile)
                HiderUtils.setLongSharedPreference(this@CameraActivity,HiderUtils.Last_File_Insert_Time,System.currentTimeMillis())
            }
            withContext(Dispatchers.Main)
            {
                setupPreviewImage()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lastPhotoVideoPath = null
    }

}