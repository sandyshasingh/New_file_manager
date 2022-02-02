package com.simplemobiletools.commons

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import java.util.ArrayList

class ExoPlayerHandeler(
    val player: ExoPlayer?,
    val data: ArrayList<String>,
    var pos: Int,
    val context: Context) : ExoPlayerListener {
    var dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
        context, Util.getUserAgent(
            context, java.lang.String.valueOf(R.string.app_name)
        )
    )

    var position = pos

    override fun playPrevious() {
        position -= 1
        //CastQueueBottomSheet.getAdapter()?.updateCurrentPlayingVideo(position)
        play()
    }

    override fun playNext() {
        position += 1
        //CastQueueBottomSheet.getAdapter()?.updateCurrentPlayingVideo(position)
        play()
    }

    override fun playCurrent() {
        play()
    }

    override fun playCurrent(seekTime: Long) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Request audio focus for playback
        val result = am.requestAudioFocus(
            focusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (player!=null && player.isPlaying)
                player.stop(true)
            if (position >= 0 && position < data.size) {

                val videoSource: MediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                        Uri.parse(data[position])
                    )
             //   (context as ExoPlayerMainActivity).title = ExoUtils.getFileNameFromUrl(data[position])
                player?.prepare(videoSource)
                player?.seekTo(seekTime)
                player?.playWhenReady = true
            } else {
                player?.stop(true)
                (context as ExoPlayerMainActivity).finish()
            }
        }
    }

    override fun playVideoAtIndex(videoFile: ListItem?, position: Int) {

    }

    private fun play() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Request audio focus for playback
        val result = am.requestAudioFocus(
            focusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (player!=null && player.isPlaying)
                player?.stop(true)
            if (position >= 0 && position < data.size) {
                val videoSource: MediaSource =
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                        Uri.parse(data[position])
                    )
              //  (context as ExoPlayerMainActivity).title = ExoUtils.getFileNameFromUrl(data[position])
                player?.prepare(videoSource)
                player?.playWhenReady = true

               // VideoHistoryDbUtility.savePlayedVideoInDB(videoList[position])

            } else {
                player?.stop(true)
                (context as ExoPlayerMainActivity).finish()
            }
        }
    }

    private val focusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                //AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.vol = 0.2f
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player?.stop()
                AudioManager.AUDIOFOCUS_LOSS -> player?.stop()
               // AudioManager.AUDIOFOCUS_GAIN -> player?.volume = 1f
                else -> {
                }
            }
        }

}

