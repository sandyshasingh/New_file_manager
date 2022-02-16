package com.rocks.addownplayer

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rocks.addownplayer.PlayerUtils.decode
import java.io.File
import java.io.FileNotFoundException

class RocksPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {


    private  var mStartTimerReceiver: StartTimerReceiver? = null
    private var audioManager: AudioManager? = null
    private var appIconId: Int? = 0
    private var notificationManager: NotificationManager? = null
    private var notification: Notification? = null
    private var viewBig: RemoteViews? = null
    private val CHANNEL_ID = "101"
    private val MAIN_CHANNEL: CharSequence = "Player Notification"

    // private static final String RADIO_MUSIC_ACTION = "RADIO_FM_SCREEN";
    private val iBinder: IBinder = LocalBinder()
    var mediaPlayer: MediaPlayer? = null
    var mPlayList: ArrayList<String>? = null
    var currentIndex: Int? = null
    var appName: String? = null
    var activityListener: ActivityListener? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        registerReceiverForHandler()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = requestAudioFocus()
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.stop()
            }
            return START_NOT_STICKY
        }
        if (intent?.action?.equals(PlayerUtils.ACTION_SET_PLAYER)!!) {
            mPlayList = intent.getStringArrayListExtra(PlayerUtils.LIST_EXTRA)
            currentIndex = intent.getIntExtra(PlayerUtils.POSITION_EXTRA, 0)
            appName = intent.getStringExtra(PlayerUtils.APP_NAME)
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.reset()
                mediaPlayer = null
            }
            setDataToPlayer()
            appIconId = getAppIconId()
            createNotification()
            if (mediaPlayer != null) {
                mediaPlayer?.setOnCompletionListener {
                    setDataToNext(true)
                }
                mediaPlayer?.start()
            }
            else
            {
                notifyPause()
                sendNegativeTimeToActivity()
            }
        } else if (intent.action?.equals(PlayerUtils.TOGGLE_PAUSE_PLAY)!!) {
            togglePlayPause()
        } else if (intent.action?.equals(PlayerUtils.STOP_SERVICE)!!) {
            destroyService()
        } else if (intent.action?.equals(PlayerUtils.ACTION_PLAY_NEXT)!!) {
            setDataToNext(false)
        } else if (intent.action?.equals(PlayerUtils.ACTION_PLAY_PREV)!!) {
            setDataToPrevious()
        }
        return START_NOT_STICKY
    }

    private fun sendNegativeTimeToActivity() {
        val intent: Intent = Intent(PlayerUtils.CURRENT_TIME)
        intent.putExtra("currentTime",-1)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        handler?.postDelayed(runnable, 1000)
    }

    private fun registerReceiverForHandler() {
        mStartTimerReceiver = StartTimerReceiver()
        val intentFilter: IntentFilter = IntentFilter(PlayerUtils.INITIATE_HANDLER)
        LocalBroadcastManager.getInstance(this).registerReceiver(mStartTimerReceiver!!, intentFilter)
    }


    inner class StartTimerReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == PlayerUtils.INITIATE_HANDLER)
            {
                if(mediaPlayer!=null && mediaPlayer?.isPlaying!!)
                    initiateHandler()
            }
        }
    }

    private fun requestAudioFocus(): Int? {
        var result: Int? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager?.requestAudioFocus(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                            AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mAudioFocusListener).build()
            )
        } else {
            result = audioManager?.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
        }
        return result
    }

    private val mAudioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (mediaPlayer != null) {
                mediaPlayer?.start()
                notifyPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS ->
                    if (mediaPlayer!=null && mediaPlayer?.isPlaying!!) {
                        mediaPlayer?.pause()
                        notifyPause()
                    }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (mediaPlayer!=null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (mediaPlayer!=null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
            }
        }
    }

    private fun destroyService() {
        activityListener?.unboundedService()
        stopForeground(true)
        stopSelf()
        stopService(Intent(applicationContext, RocksPlayerService::class.java))
    }

    private fun getAppIconId(): Int? {
        return R.drawable.callock_launcher
    }

    private fun createNotification() {
        //Notification
        viewBig = RemoteViews(this.packageName, R.layout.notification_youtube)
        val doThings = Intent(this, RocksPlayerService::class.java)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, MAIN_CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "Player Notifications"
            notificationChannel.enableVibration(false)
            notificationChannel.enableLights(false)
            notificationChannel.setSound(null, null)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
        @SuppressLint("WrongConstant") val builder: NotificationCompat.Builder = appIconId?.let {
            NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(it)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentTitle("")
                    .setContent(viewBig)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setContentText("")
                    .setAutoCancel(false)
        }!!
        builder.setCustomBigContentView(viewBig)
        notification = builder.build()
        // setImageTitleAuthor(VID_ID)
        viewBig?.setTextViewText(R.id.title, decode(File(mPlayList?.get(currentIndex?:0)?:"").name,offset))
        if (viewBig != null) {
            viewBig?.setOnClickPendingIntent(R.id.stop_service,
                    PendingIntent.getService(applicationContext, 0, doThings.setAction(PlayerUtils.STOP_SERVICE), 0))
            viewBig?.setOnClickPendingIntent(R.id.pause_play_video, PendingIntent.getService(applicationContext, 0,
                    doThings.setAction(PlayerUtils.TOGGLE_PAUSE_PLAY), 0))
            viewBig?.setOnClickPendingIntent(R.id.next_video,
                    PendingIntent.getService(applicationContext, 0,
                            doThings.setAction(PlayerUtils.ACTION_PLAY_NEXT), 0))
            viewBig?.setOnClickPendingIntent(R.id.previous_video,
                    PendingIntent.getService(applicationContext, 0,
                            doThings.setAction(PlayerUtils.ACTION_PLAY_PREV), 0))
            val intent1 = Intent("CALLOCK_AUDIO_PLAYER")
            //intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            notification?.contentIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT)

            //Start Foreground Service
            startForeground(PlayerUtils.NOTIFICATION_ID, notification)
        }
    }

    var handler: Handler? = null

    var runnable = Runnable {
        sendCurrentTimeToActivity()
    }

    private fun sendCurrentTimeToActivity() {
        val intent: Intent = Intent(PlayerUtils.CURRENT_TIME)
        intent.putExtra("currentTime", mediaPlayer?.currentPosition)
        if(applicationContext!=null) {
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
        handler?.postDelayed(runnable, 1000)
    }

    private fun initiateHandler() {
        if(handler== null)
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(runnable, 1000)
    }

    fun togglePlayPause() {
        if(mediaPlayer!=null)
        {
            if (mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
                handler?.removeCallbacks(runnable)
                audioManager?.abandonAudioFocus(mAudioFocusListener)
            } else {
                requestAudioFocus()
                notifyPlay()
                if(mediaPlayer!=null)
                {
                    mediaPlayer?.start()
                    initiateHandler()
                }
            }
        }
    }

    private fun notifyPlay() {
        activityListener?.onPlay()
        viewBig?.setImageViewResource(R.id.pause_play_video, R.drawable.ic_pause_black)
        notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
    }

    private fun notifyPause() {
        activityListener?.onPaused()
        viewBig?.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play_black)
        notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
    }

    private fun setDataToPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(currentIndex?.let { mPlayList?.get(it) })
            mediaPlayer?.prepare()
            sendDurationToActivity()
            fetchDetails()
        } catch (e: Exception) {
            mediaPlayer = null
            Toast.makeText(applicationContext, "something went wrong with this file", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendDurationToActivity() {
        val intent: Intent = Intent(PlayerUtils.DURATION)
        intent.putExtra("duration", mediaPlayer?.duration!!)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }


    fun setDataToNext(onCompleted: Boolean) {
        var startAgain = false
        currentIndex = currentIndex?.plus(1)!!
        if (mPlayList?.size!! > currentIndex!!) {
            if (mediaPlayer != null) {
                if (!onCompleted && mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.stop()
                    handler?.removeCallbacks(runnable)
                    startAgain = true
                } else if (onCompleted) {
                    startAgain = true
                }
                    mediaPlayer?.reset()
            } else {
                mediaPlayer = MediaPlayer()
            }
            try {
                mediaPlayer?.setDataSource(currentIndex?.let { mPlayList?.get(it) })
                mediaPlayer?.prepare()
                PlayerUtils.positionInList = currentIndex
                sendDurationToActivity()
                fetchDetails()
                viewBig?.setTextViewText(R.id.title, decode(File(mPlayList?.get(currentIndex!!)!!).name,
                    offset))
                notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                if (startAgain) {
                    mediaPlayer?.start()
                }
            } catch (e: java.lang.Exception) {
                mediaPlayer = null
                activityListener?.onErrorInData()
                notifyPause()
            }
        } else {
            currentIndex = currentIndex?.minus(1)
            if (!onCompleted)
                Toast.makeText(applicationContext, "no next song to play", Toast.LENGTH_SHORT).show()
            else {
                handler?.removeCallbacks(runnable)
                activityListener?.onPaused()
                viewBig?.setImageViewResource(R.id.pause_play_video, R.drawable.ic_play_black)
                notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
            }
        }
    }

    private fun fetchDetails() {
        Log.d("reached","fetchDetails")
                var thumbnail: Uri? = null
                var artist: String? = null

                val albumArtUri = Uri.parse("content://media/external/audio/albumart")
                val c: Cursor = applicationContext.contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf<String>(
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ALBUM_ID
                ), MediaStore.Audio.Media.DATA.toString() + " = ?", arrayOf(
                        currentIndex?.let { mPlayList?.get(it) }
                ),
                        "")!!
        var artwork: Bitmap? = null
        if (c!=null && c.moveToFirst()) {
                    thumbnail = ContentUris.withAppendedId(albumArtUri, c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
                    artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    if (thumbnail != null) {
                        try {
                            val pfd = applicationContext.contentResolver.openFileDescriptor(thumbnail, "r")
                            if (pfd != null) {
                                val fd = pfd.fileDescriptor
                                artwork = BitmapFactory.decodeFileDescriptor(fd)
                            }
                            viewBig?.setImageViewUri(R.id.thumbnail, thumbnail)
                            notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                        } catch (e: FileNotFoundException) {
                            val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
                            artwork = drawableToBitmap(d)
                            viewBig?.setImageViewResource(R.id.thumbnail, R.drawable.music_place_holder)
                            notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                        }
                    }
                    else
                    {
                       /* val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
                        artwork = drawableToBitmap(d)*/
                        viewBig?.setImageViewResource(R.id.thumbnail, R.drawable.music_place_holder)
                        notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                    }
                }
        else {
           /* val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
            artwork = drawableToBitmap(d)*/
            viewBig?.setImageViewResource(R.id.thumbnail, R.drawable.music_place_holder)
            notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                }
        val intent: Intent? = Intent(PlayerUtils.DETAILS)
        intent?.putExtra(PlayerUtils.THUMBNAIL_EXTRA, artwork)
        intent?.putExtra(PlayerUtils.ARTIST_EXTRA, artist)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent!!)
        PlayerUtils.artwork = artwork
        PlayerUtils.artist = artist
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        try
        {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth!!, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        catch(e : Exception)
        {
            return null
        }
    }

    fun setDataToPrevious() {
        var startAgain = false
        if (currentIndex!! > 0 && mPlayList?.size!! > 1) {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying!!) {
                    mediaPlayer?.stop()
                    handler?.removeCallbacks(runnable)
                    startAgain = true
                }
                mediaPlayer?.reset()
            } else {
                mediaPlayer = MediaPlayer()
            }
            currentIndex = currentIndex?.minus(1)
            try {
                mediaPlayer?.setDataSource(currentIndex?.let { mPlayList?.get(it) })
                mediaPlayer?.prepare()
                PlayerUtils.positionInList = currentIndex
                sendDurationToActivity()
                viewBig?.setTextViewText(R.id.title, decode(File(mPlayList?.get(currentIndex!!)!!).name,offset))
                notificationManager?.notify(PlayerUtils.NOTIFICATION_ID, notification)
                fetchDetails()
                if (startAgain) {
                    mediaPlayer?.start()
                }
            } catch (e: java.lang.Exception) {
                mediaPlayer = null
                activityListener?.onErrorInData()
                notifyPause()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerUtils.mService = null
        if (mediaPlayer!=null && mediaPlayer?.isPlaying!!) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer = null
            handler?.removeCallbacks(runnable)
            handler = null
        }
        if (audioManager != null && mAudioFocusListener != null) {
            audioManager?.abandonAudioFocus(mAudioFocusListener!!)
        }
        if(mStartTimerReceiver!=null)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mStartTimerReceiver!!)
            mStartTimerReceiver = null
        }
    }

    inner class LocalBinder : Binder() {

        fun getService(): RocksPlayerService {
            return this@RocksPlayerService
        }

    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (mediaPlayer != null) {
                mediaPlayer?.start()
                notifyPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS -> if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (mediaPlayer != null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.pause()
                notifyPause()
            }
        }
    }
}
