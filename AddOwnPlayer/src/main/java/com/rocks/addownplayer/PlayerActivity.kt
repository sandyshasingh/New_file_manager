package com.rocks.addownplayer

import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import com.rocks.addownplayer.PlayerUtils.decode
import kotlinx.android.synthetic.main.activity_player.*
import java.io.File
import java.util.concurrent.TimeUnit

const val offset = 17


class PlayerActivity : AppCompatActivity(), ActivityListener {

    private var mDurationTimeReceiver: DurationReceiver? = null
    private var mCurrentTimeReceiver: CurrentTimeReceiver? = null
    private var mDetailsReceiver: DetailsReceiver? = null

    var pathList: ArrayList<String>? = null
    var positionInList: Int? = 0
    var appName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_player)
        setListeners()
        if (intent.hasExtra(PlayerUtils.LIST_EXTRA)) {
            img_play_pause?.setImageResource(R.drawable.pause)
            pathList = intent.extras?.get(PlayerUtils.LIST_EXTRA) as ArrayList<String>
            positionInList = intent.extras?.getInt(PlayerUtils.POSITION_EXTRA, 0)
            appName = intent.extras?.getString(PlayerUtils.APP_NAME)
            PlayerUtils.pathList = pathList
            PlayerUtils.positionInList = positionInList
            PlayerUtils.appName = appName
            val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
            PlayerUtils.artwork = null
            setDetailsOnViews("",null)
            if (pathList != null) {
                songName?.text = decode(File(pathList?.get(positionInList!!)!!).name,offset)
                bindServiceAndPlay()
            }
        } else if (PlayerUtils.mService != null) {
            pathList = PlayerUtils.pathList
            positionInList = PlayerUtils.positionInList
            appName = PlayerUtils.appName
            val currentTime = PlayerUtils.mService?.mediaPlayer?.duration!!
            seekbar?.max = currentTime
            tv_right.text = TimeConversionInMinsec(currentTime)
            if (PlayerUtils.mService?.mediaPlayer?.isPlaying!!) {
                img_play_pause?.setImageResource(R.drawable.pause)
            } else {
                img_play_pause?.setImageResource(R.drawable.ic_play)
            }
            songName?.text = decode(File(pathList?.get(positionInList!!)!!).name, offset)
            setDetailsOnViews(PlayerUtils.artist, PlayerUtils.artwork)
            bindServiceAndDisplay()
        } else {
            Toast.makeText(this, "no songs to play", Toast.LENGTH_LONG).show()
            finish()
        }
        mCurrentTimeReceiver = CurrentTimeReceiver()
        val intentFilter: IntentFilter = IntentFilter(PlayerUtils.CURRENT_TIME)
        LocalBroadcastManager.getInstance(this).registerReceiver(mCurrentTimeReceiver!!, intentFilter)

        mDurationTimeReceiver = DurationReceiver()
        val intentFilter1: IntentFilter = IntentFilter(PlayerUtils.DURATION)
        LocalBroadcastManager.getInstance(this).registerReceiver(mDurationTimeReceiver!!, intentFilter1)

        mDetailsReceiver = DetailsReceiver()
        val intentFilter2: IntentFilter = IntentFilter(PlayerUtils.DETAILS)
        LocalBroadcastManager.getInstance(this).registerReceiver(mDetailsReceiver!!, intentFilter2)
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        try {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth!!, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        catch (e : Exception)
        {
            return null
        }
    }




    private fun setListeners() {
        back_button?.setOnClickListener()
        {
            finish()
        }
        img_previous?.setOnClickListener()
        {
            PlayerUtils.mService?.setDataToPrevious()
        }
        img_next?.setOnClickListener()
        {
            PlayerUtils.mService?.setDataToNext(false)
        }
        img_play_pause?.setOnClickListener()
        {
            PlayerUtils.mService?.togglePlayPause()
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                PlayerUtils.mService?.mediaPlayer?.seekTo(seekBar?.progress!!)
            }

        })
    }

    private fun bindServiceAndDisplay() {
        val intent = Intent(this, RocksPlayerService::class.java)
        this.bindService(intent, mConnection, BIND_ADJUST_WITH_ACTIVITY)
    }


    val mConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            PlayerUtils.mService = (iBinder as RocksPlayerService.LocalBinder).getService()
            PlayerUtils.mService?.activityListener = this@PlayerActivity
            if (PlayerUtils.mService?.mediaPlayer != null) {
                if (PlayerUtils.mService?.mediaPlayer?.isPlaying!!) {
                    img_play_pause?.setImageResource(R.drawable.pause)
                } else {
                    img_play_pause?.setImageResource(R.drawable.ic_play)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            PlayerUtils.mService?.activityListener = null
        }
    }

    private fun bindServiceAndPlay() {
        val intent = Intent(this, RocksPlayerService::class.java)
        intent.action = PlayerUtils.ACTION_SET_PLAYER
        intent.putStringArrayListExtra(PlayerUtils.LIST_EXTRA, pathList)
        intent.putExtra(PlayerUtils.POSITION_EXTRA, positionInList)
        intent.putExtra(PlayerUtils.APP_NAME, appName)
        startService(intent)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }


    fun TimeConversionInMinsec(milisec: Int): String? {
        val millis = milisec.toLong()
        return if (milisec >= 3600000) {
            timeConversionInHHMMSS(milisec.toLong())
        } else {
            String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            )
        }
    }

    fun timeConversionInHHMMSS(millis: Long): String {
        return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
    }

    override fun onErrorInData() {
        img_play_pause?.setImageResource(R.drawable.ic_play)
        tv_right.text = TimeConversionInMinsec(0)
        tv_left.text = TimeConversionInMinsec(0)
        seekbar?.progress = 0
        val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
        PlayerUtils.artwork = null
        setDetailsOnViews("",PlayerUtils.artwork)
    }


    override fun onPaused() {
        img_play_pause?.setImageResource(R.drawable.ic_play)
    }

    override fun onPlay() {
        img_play_pause?.setImageResource(R.drawable.pause)
    }

    override fun unboundedService() {
        finish()
    }

    /*override fun onDetailFetched(thumbnailUri: Uri, artist: String) {
        tv_artist?.text = artist
        val requestOptions = RequestOptions().placeholder(R.drawable.music_place_holder)
        Glide.with(this).asBitmap().optionalCenterCrop()
                .load(thumbnailUri).thumbnail(0.04f).apply(requestOptions)
                .into(img_thumbnail)
    }*/



    inner class CurrentTimeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentTime = intent?.getIntExtra("currentTime", 0)
            if (currentTime!! >= 0) {
                seekbar?.progress = currentTime
                tv_left?.text = TimeConversionInMinsec(currentTime)
            } else {
                tv_left?.text = TimeConversionInMinsec(0)
                onPaused()
            }
        }
    }

    inner class DetailsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val artist = intent?.getStringExtra(PlayerUtils.ARTIST_EXTRA)
            val artwork = intent?.extras?.get(PlayerUtils.THUMBNAIL_EXTRA) as Bitmap?
            setDetailsOnViews(artist, artwork)
        }
    }

    private fun setDetailsOnViews(artist: String?, artwork: Bitmap?) {
        if (artist != null)
            tv_artist?.text = artist
        if (artwork != null) {
            img_thumbnail?.setImageBitmap(artwork)
            val palette = Palette.from(artwork).generate()
            val defaultValue = Color.BLACK
            val mutedLight: Int = palette.getLightMutedColor(defaultValue)
            var darkColor = Color.BLUE
            try {
                darkColor = Plate.getColor(palette, true)!!
            } catch (e: Exception) {
            }
            val gd = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(mutedLight, darkColor))
            gd.cornerRadius = 5f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // window.setBackgroundDrawable(gd)
                holder?.background = gd
                window.statusBarColor = mutedLight
                window.navigationBarColor = darkColor
            }
            /* Palette.from(artwork).generate(Palette.PaletteAsyncListener { palette: Palette? ->

                // img_backGround?.setImageDrawable(gd)
             })*/
            val intent: Intent = Intent(PlayerUtils.INITIATE_HANDLER)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
        else
        {
            val d = ResourcesCompat.getDrawable(resources, R.drawable.music_place_holder, null)
            val artwork = drawableToBitmap(d)
            if(artwork != null)
            {
                img_thumbnail?.setImageBitmap(artwork)
                PlayerUtils.artwork = null
                val palette = Palette.from(artwork!!).generate()
                val defaultValue = Color.BLACK
                //val mutedLight: Int = palette.getLightMutedColor(defaultValue)
                var darkColor = Color.BLUE
                try {
                    darkColor = Plate.getColor(palette, true)!!
                } catch (e: Exception) {
                }
                val gd = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(defaultValue, darkColor))
                gd.cornerRadius = 5f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // window.setBackgroundDrawable(gd)
                    holder?.background = gd
                    window.statusBarColor = defaultValue
                    window.navigationBarColor = darkColor
                }
            }
            else
            {
                img_thumbnail?.setImageResource(R.drawable.music_place_holder)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // window.setBackgroundDrawable(gd)
                    holder?.background = ResourcesCompat.getDrawable(resources,R.drawable.radio_player_background,null)
                    window.statusBarColor = ResourcesCompat.getColor(resources,R.color.startFmPlayer,null)
                    window.navigationBarColor = ResourcesCompat.getColor(resources,R.color.endFmColor,null)
                }
            }
            /* Palette.from(artwork).generate(Palette.PaletteAsyncListener { palette: Palette? ->

                // img_backGround?.setImageDrawable(gd)
             })*/
            val intent: Intent = Intent(PlayerUtils.INITIATE_HANDLER)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    inner class DurationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentTime = intent?.getIntExtra("duration", 0)
            Log.d("received", "duration $currentTime")
            seekbar.max = currentTime!!
            tv_right.text = TimeConversionInMinsec(currentTime)
            positionInList = PlayerUtils.positionInList
            songName?.text = decode(File(pathList?.get(positionInList!!)!!).name, offset)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            if(mConnection!=null) {
                unbindService(mConnection)
            }
        }catch (e:Exception){

        }

        //unbindService(mConnection)
        if (mCurrentTimeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mCurrentTimeReceiver!!)
            mCurrentTimeReceiver = null
        }
        if (mDurationTimeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDurationTimeReceiver!!)
            mDurationTimeReceiver = null
        }
        if (mDetailsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDetailsReceiver!!)
            mDetailsReceiver = null
        }
    }

}