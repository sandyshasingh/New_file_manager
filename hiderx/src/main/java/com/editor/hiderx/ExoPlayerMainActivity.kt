package com.editor.hiderx

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.editor.hiderx.HiderUtils.getActivityIsAlive
import com.editor.hiderx.activity.CalculatorActivity
import com.editor.hiderx.database.HiddenFiles
import com.editor.hiderx.database.HiddenFilesDatabase
import com.editor.hiderx.dataclass.FileDataClass
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.rocks.chromecast.exoplayer.ExoPlayerHandeler
import kotlinx.android.synthetic.main.custom_exo_controller_view.*

import kotlinx.android.synthetic.main.exo_player_main_activity.*
import kotlinx.android.synthetic.main.unhide_path_dialog.view.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.max


class ExoPlayerMainActivity : AppCompatActivity(), PlayerControlView.VisibilityListener,CoroutineScope by MainScope() {


    var pressedBack: Boolean = false
    private var mIntent: Intent? = null
    private var hiddenFile: HiddenFiles? = null
    private var fileDataClass: FileDataClass? = null
    private var exoPrevious: ImageView? = null
    private var exoNext: ImageView? = null
    private var play: ImageView? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var listOfUrl: ArrayList<String> = ArrayList()
    private var positionOfVideoToPlay = 0
    private var audioManager: AudioManager? = null

    private var defaultTimeBar: DefaultTimeBar? = null
    private var exoPlayerHandeler: ExoPlayerHandeler? = null
    private var isPlayerIsIdle = false
    private var seekbarPosition: Long = 0
    private var changeAspectRatio: ImageView? = null
    private var mIconsResize =
        intArrayOf(R.drawable.ic_screen_fit, R.drawable.ic_streach, R.drawable.ic_crop)
    private var mResizeTile = arrayOf("FIT", "STRETCH", "CROP")

    // private val mResourceProvider = ResourceProvider.instance

    private var counter = 1
    private var aspectRatios = intArrayOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    )
    private var resumePosition = 0L

    private var mAppProgressDialog: AppProgressDialog? = null
   // val networkStream = false
    var videoList: List<HiddenFiles>? = null
    var filesList: List<FileDataClass>? = null


    // private var mCastSession: CastSession? = null
//    private var mediaRouteMenuItem: MenuItem? = null
    //  private var mCastContext: CastContext? = null
    //   private var mIntroductoryOverlay: IntroductoryOverlay? = null
    //private val mSessionManagerListener: SessionManagerListener<CastSession> = MySessionManagerListener()

    //  private var deviceIpAddress: String? = null

    private var activity: Activity? = null


    /*inner class MySessionManagerListener : SessionManagerListener<CastSession> {
        override fun onSessionEnded(session: CastSession, error: Int) {
            if (session === mCastSession) {
                mCastSession = null
            }
            invalidateOptionsMenu()
        }

        override fun onSessionResumed(
            session: CastSession,
            wasSuspended: Boolean
        ) {
            mCastSession = session
            invalidateOptionsMenu()
        }

        override fun onSessionStarted(
            session: CastSession,
            sessionId: String
        ) {
            mCastSession = session
            invalidateOptionsMenu()
            if(activity!=null) {
                val intent = Intent(activity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                finish()
                playVideoOnCast()
            }
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionStartFailed(session: CastSession, error: Int) {
            invalidateOptionsMenu()
        }

        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(
            session: CastSession,
            sessionId: String
        ) {
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            invalidateOptionsMenu()
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {}
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    private fun initializeCast() {
        SimpleWebServer.init(this, BuildConfig.DEBUG)
        mCastContext?.addCastStateListener { newState: Int ->
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                showIntroductoryOverlay()
            }
        }
        mCastContext?.sessionManager?.addSessionManagerListener(
            mSessionManagerListener, CastSession::class.java
        )
        if (mCastSession == null) {
            mCastSession = CastContext.getSharedInstance(this).sessionManager
                .currentCastSession
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = Color.TRANSPARENT
            window?.navigationBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.exo_player_main_activity)

       // toolbar?.setBackgroundResource(R.drawable.gradient_reverse_bg)
      //  setSupportActionBar(toolbar)
      //  supportActionBar?.setDisplayHomeAsUpEnabled(true)
        /*toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }*/
        btn_back?.setOnClickListener()
        {
            onBackPressed()
        }

        img_unhide?.setOnClickListener()
        {
            showUnhidePathDialog()
        }

        activity = this

        //    mCastContext = CastContext.getSharedInstance(this)

        positionOfVideoToPlay = intent.getIntExtra("pos", 0)

        VideoDataHolder.data?.let { videoList = it }
        VideoDataHolder.filesData?.let { filesList = it }
        if (videoList != null) {
            for (i in videoList?.indices!!)
                listOfUrl.add(videoList?.get(i)?.path!!)
        }
        else if(filesList != null)
        {
            for (i in filesList?.indices!!)
                listOfUrl.add(filesList?.get(i)?.path!!)
        }
        playerView = findViewById(R.id.playerView)
        defaultTimeBar = findViewById(R.id.exo_progress)
        play = findViewById(R.id.exo_play)

        playerView?.setControllerVisibilityListener(this)

        player = ExoPlayer.Builder(this).apply {
            setSeekBackIncrementMs(10000)
            setSeekForwardIncrementMs(10000)
        }.build()

        exoPlayerHandeler = ExoPlayerHandeler(
            player!!, listOfUrl, positionOfVideoToPlay, this@ExoPlayerMainActivity
        )
        playerView?.player = player
        changeAspectRatio = findViewById(R.id.changeAspectRatio)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        changeAspectRatio?.setOnClickListener {
            counter++
            changeScreenSize()
        }
        try {
            //getResumePosition()
            exoPlayerHandeler?.playCurrent(resumePosition)
        } catch (e: Exception) {
            // AppLog.showInfo("video play issue", "cannot play the video")
        }
        exoPrevious = findViewById(R.id.exo_prev)
        exoPrevious?.setOnClickListener {
            exoPlayerHandeler?.playPrevious()
        }
        exoNext = findViewById(R.id.next)
        exoNext?.setOnClickListener {
            exoPlayerHandeler?.playNext()
        }

        play?.setOnClickListener {
            startPlayer()
        }
        player?.addListener(object : Player.EventListener {
            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_ENDED -> exoPlayerHandeler?.playNext()
                    Player.STATE_IDLE -> {
                        isPlayerIsIdle = true
                        seekbarPosition = player!!.currentPosition
                    }
                    Player.STATE_READY -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@ExoPlayerMainActivity, "Video not supported", Toast.LENGTH_LONG)
                    .show()
                // mResourceProvider.showErrorToast("Video not supported")
                exoPlayerHandeler?.playNext()
//                finish()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })

        /*  findViewById<AppCompatImageButton>(R.id.playlist)?.setOnClickListener {
           // CastQueueBottomSheet.show(this,this)
          //  CastQueueBottomSheet.getAdapter()?.updateCurrentPlayingVideo(exoPlayerHandeler!!.position)
        }*/

        /* window?.decorView?.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility == 7) {
                val params = controller?.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                controller?.layoutParams = params
            } else {
                val params = controller?.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, getNavBarHeight(this))
                controller?.layoutParams = params
            }
        }*/

    }

    /* override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.expanded_controller, menu)

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.media_route_menu_item)
        val mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item)
        val mediaActionProvider = MenuItemCompat.getActionProvider(mediaRouteMenuItem) as MediaRouteActionProvider
        mediaActionProvider.setAlwaysVisible(true)
        showIntroductoryOverlay()

        return super.onCreateOptionsMenu(menu)
    }*/

    private fun showUnhidePathDialog() {
        pausePlayer()
        val view1 = layoutInflater.inflate(R.layout.unhide_path_dialog, null)
        var container : AlertDialog? = null
        val dialog = AlertDialog.Builder(this)
        view1?.tv_unhide_path?.text = getString(R.string.unhide_path_prefix)+ PUBLIC_DIRECTORY_HIDERBACKUP_FOR_PHOTOS
        view1?.tv_cancel_unhide?.setOnClickListener()
        {
            container?.dismiss()
        }
        view1?.tv_unhide?.setOnClickListener()
        {
            if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_default)
            {
                unhideSelectedFiles(false)
            }
            else if(view1.path_to_unhide?.checkedRadioButtonId == R.id.path_original)
            {
                unhideSelectedFiles(true)
            }
            container?.dismiss()
        }
        dialog.setView(view1)
        container=dialog.show()
    }

    private fun unhideSelectedFiles(isOriginalPath: Boolean) {
        launch{
            val operation = async(Dispatchers.IO){
                videoList.let{ lt ->
                    exoPlayerHandeler?.position?.let{
                        hiddenFile = lt?.get(it)
                    }
                }
                filesList.let{ lt ->
                    exoPlayerHandeler?.position?.let{
                       fileDataClass  = lt?.get(it)
                    }
                }
                val mediaScanner = MediaScanner(this@ExoPlayerMainActivity)
                var moved = false
                var newExternalPath : String? = null
                val externalStoragePublic: File? = StorageUtils.getPublicAlbumStorageDirForPhotos()
                val hiddenFilesDatabase: HiddenFilesDatabase? = HiddenFilesDatabase.getInstance(this@ExoPlayerMainActivity)
                if(hiddenFilesDatabase == null || !isOriginalPath) {
                    hiddenFile?.let {
                        newExternalPath = externalStoragePublic?.path + "/" + it.name }
                    fileDataClass?.let {
                        newExternalPath = externalStoragePublic?.path + "/" + it.name }
                }
                else {
                    var originalPath : String? = ""
                    hiddenFile?.let{originalPath= hiddenFilesDatabase.hiddenFilesDao.getOriginalPathForFile(it.path)
                        newExternalPath = if(TextUtils.isEmpty(originalPath))
                            externalStoragePublic?.path + "/" + it.name
                        else
                            originalPath
                    }
                    fileDataClass?.let{originalPath= hiddenFilesDatabase.hiddenFilesDao.getOriginalPathForFile(it.path)
                        newExternalPath = if(TextUtils.isEmpty(originalPath))
                            externalStoragePublic?.path + "/" + it.name
                        else
                            originalPath
                    }
                }
                hiddenFile?.let {moved = StorageUtils.move(it.path, newExternalPath!!)
                    if(moved)
                    {
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                        mediaScanner?.scan(newExternalPath)
                    }
                }
                fileDataClass?.let {moved = StorageUtils.move(it.path, newExternalPath!!)
                    if(moved)
                    {
                        hiddenFilesDatabase?.hiddenFilesDao?.deleteFile(it.path)
                        mediaScanner?.scan(newExternalPath)
                    }
                }
            }
            operation.await()
            withContext(Dispatchers.Main)
            {
                if(filesList != null)
                {
                    exoPlayerHandeler?.position?.let{
                        filesList = filesList?.drop(it)
                        exoPlayerHandeler?.data?.removeAt(it)
                    }
                }
                if(videoList != null)
                {
                    exoPlayerHandeler?.position?.let{
                        videoList = videoList?.drop(it)
                        exoPlayerHandeler?.data?.removeAt(it)
                    }
                }
                exoPlayerHandeler?.playCurrent()
               /* fileDataClass?.let{onPagerItemsClickLister?.onFileRemoved(it)}
                hiddenFile?.let{onPagerItemsClickLister?.onItemRemoved(it)}*/

            }
        }
    }


    private fun changeScreenSize() {
        when (counter) {
            1 -> {
                playerView?.resizeMode = aspectRatios[0]
                changeAspectRatio?.setImageResource(mIconsResize[0])

                Toast.makeText(this@ExoPlayerMainActivity,mResizeTile[0], Toast.LENGTH_SHORT).show()
                // mResourceProvider.showSuccessToast(mResizeTile[0])
            }
            2 -> {
                playerView?.resizeMode = aspectRatios[1]
                changeAspectRatio?.setImageResource(mIconsResize[1])
//                Toasty.success(this@ExoPlayerMainActivity, mResizeTile[1]).show()
                Toast.makeText(this@ExoPlayerMainActivity,mResizeTile[1], Toast.LENGTH_SHORT).show()

                //  mResourceProvider.showSuccessToast(mResizeTile[1])
            }
            3 -> {
                playerView?.resizeMode = aspectRatios[2]
                changeAspectRatio?.setImageResource(mIconsResize[2])
//                Toasty.success(this@ExoPlayerMainActivity, mResizeTile[2]).show()
                Toast.makeText(this@ExoPlayerMainActivity,mResizeTile[2], Toast.LENGTH_SHORT).show()

                // mResourceProvider.showSuccessToast(mResizeTile[2])
                counter = 0
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        pausePlayer()
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        updateBookmarkHashMap()
        if(!pressedBack)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR,true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


    private fun pausePlayer() {
        player?.playWhenReady = false
    }

    private fun startPlayer() {
        player?.playWhenReady = true
        if (isPlayerIsIdle) {
            exoPlayerHandeler?.playCurrent(seekbarPosition)
            isPlayerIsIdle = false
        }
    }

    private fun stopPlayer() {
        if (player != null && player!!.isPlaying)
            player?.stop(true)
    }

    override fun onResume() {
        hideSystemUi()
        //  initializeCast()
        super.onResume()
        pressedBack = false
        if(mIntent!=null)
        {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayer()
    }

    private fun onVideoFragmentInteraction(VISIBILITY: Int) {
        if (VISIBILITY == View.VISIBLE) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                try {
                    toolbar?.animate()?.alpha(1f)?.setInterpolator(DecelerateInterpolator())
                        ?.start()
                    toolbar?.visibility = View.VISIBLE
                } catch (e: java.lang.Exception) {
                    if (toolbar != null && toolbar?.visibility == View.VISIBLE)
                        toolbar?.visibility = View.INVISIBLE
                }
            }, 300)
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                try {
                    hideSystemUi()
                    toolbar?.animate()?.alpha(0f)?.setInterpolator(DecelerateInterpolator())
                        ?.start()
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({ toolbar?.visibility = View.GONE }, 50)
                } catch (e: java.lang.Exception) {
                    if (toolbar != null && toolbar?.visibility == View.INVISIBLE) {
                        toolbar?.visibility = View.VISIBLE
                    }
                }
            }, 300)
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onVisibilityChange(visibility: Int) {
        onVideoFragmentInteraction(visibility)
    }
    private fun updateBookmarkHashMap() {
        updateResumePosition()
        val position = exoPlayerHandeler!!.position
      /*  if(position < videoList.size && position != -1) {
            videoList[position].lastPlayedDuration = resumePosition
         //   VideoHistoryDbUtility.savePlayedVideoInDB(videoList[position])
         //   ChromeCastDataHolder.getData()[position].lastPlayedDuration = resumePosition
        }*/
    }

    private fun updateResumePosition() {
        if (player != null) {
            resumePosition =
                if (player?.isCurrentWindowSeekable == true)
                    max(0, player!!.currentPosition)
                else
                    C.TIME_UNSET
        }
    }

   /* private fun getResumePosition() {
        resumePosition = try {
            videoList[exoPlayerHandeler!!.position].lastPlayedDuration
        } catch (e: java.lang.Exception) {
            0
        }
    }
*/

    private fun dismissDialog() {
        if (this != null && mAppProgressDialog != null && mAppProgressDialog!!.isShowing && getActivityIsAlive(this)) {
            mAppProgressDialog?.dismiss()
            mAppProgressDialog = null
        }
    }
}

    /* fun playVideoOnCast() {
         *//** Find the IpAddress of the device and save it to [deviceIpAddress]
         * so that Service class can pick it up to create a small http server  *//*
        showDialog()
        deviceIpAddress = ChromeCastUtils.findIPAddress(this)
        if (deviceIpAddress == null) {
            mResourceProvider.showNormalToast("Connect to a wifi device or hotspot")
            return
        }
        ChromeCastUtils.deviceIpAddress = deviceIpAddress
        *//** Start a http server.  *//*
        startService(Intent(this, WebService::class.java))
        *//** Play the file on the device.  *//*
        var remoteMediaClient: RemoteMediaClient? = null
        remoteMediaClient = if (mCastSession != null && mCastSession!!.remoteMediaClient != null) {
            mCastSession?.remoteMediaClient
        } else {
            return
        }
        remoteMediaClient?.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                super.onStatusUpdated()
                dismissDialog()
            }
        })
        if (videoList != null  && videoList.isNotEmpty()) {
            if (networkStream) {
                val videofileInfo: VideoFileInfo = videoList[0]
                remoteMediaClient?.load(
                    MediaLoadRequestData.Builder()
                        .setMediaInfo(ChromeCastUtils.buildMediaInfoForStream(videofileInfo))
                        *//** Use the [MediaInfo] generated from [buildMediaInfo].  *//*
                        .setAutoplay(true)
                        .build()
                )
            } else {
                val newItemArray =
                    arrayOfNulls<MediaQueueItem>(videoList.size)
                for (i in videoList.indices) {
                    val videoFileInfo: VideoFileInfo = videoList[i]
                    val queueItem: MediaQueueItem =
                        MediaQueueItem.Builder(ChromeCastUtils.buildMediaInfo(videoFileInfo)).setAutoplay(true)
                            .setPreloadTime(20.0).build()
                    newItemArray[i] = queueItem
                }
                remoteMediaClient?.queueLoad(
                    newItemArray, exoPlayerHandeler!!.position,
                    MediaStatus.REPEAT_MODE_REPEAT_OFF, null
                )
            }
        }
*/

        /* if (mViewModel != null && videoList != null) {
            if(networkStream){
                VideoFileInfo videofileInfo = videoList.get(0);
                remoteMediaClient.load(new MediaLoadRequestData.Builder()
                        .setMediaInfo(CastUtils.buildMediaInfoForStream(videofileInfo))
                        */
        /** Use the [MediaInfo] generated from [buildMediaInfo].  */ /*
                        .setAutoplay(true)
                        .build()
                );
            }else {
                VideoFileInfo videoFileInfo = videoList.get(currentVideoPosition);
                String subtitlePath = getSubtitlePathFromPref(currentVideoPosition);

                remoteMediaClient.load(new MediaLoadRequestData.Builder()
                        .setMediaInfo(buildMediaInfo(videoFileInfo, subtitlePath))
                        */
        /** Use the [MediaInfo] generated from [buildMediaInfo].  */ /*
                        .setAutoplay(true)
                        .setCurrentTime(resumePosition)
                        .build()
                );
            }
        }*/


   /* private fun showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay?.remove()
        }
        if (mediaRouteMenuItem != null && mediaRouteMenuItem!!.isVisible) {
            Handler(Looper.getMainLooper()).post {
                mIntroductoryOverlay = IntroductoryOverlay.Builder(
                    this@ExoPlayerMainActivity, mediaRouteMenuItem!!
                )
                    .setTitleText(getString(R.string.cast_title))
                    .setOverlayColor(R.color.app_color)
                    .setSingleTime()
                    .setOnOverlayDismissedListener { mIntroductoryOverlay = null }
                    .build()
                mIntroductoryOverlay?.show()
            }
        }
    }
*/


    /*private fun showDialog() {
        if (mAppProgressDialog == null && getActivityIsAlive(this@ExoPlayerMainActivity)) {
            mAppProgressDialog = AppProgressDialog(this)
            mAppProgressDialog?.setCancelable(true)
            mAppProgressDialog?.setCanceledOnTouchOutside(false)
            mAppProgressDialog?.show()
        }
    }

    override fun onVisibilityChange(visibility: Int) {
        onVideoFragmentInteraction(visibility)
    }

    override fun playVideoAtIndex(videoFile: HiddenFiles?, position: Int) {
        exoPlayerHandeler?.position = position
        exoPlayerHandeler?.playCurrent()
    }*/
