package com.github.odaridavid.zikk.playback

/**
 *
 * Copyright 2020 David Odari
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *            http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 **/
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import com.github.odaridavid.zikk.utils.versionFrom

/**
 * @see <a href="https://developer.android.com/guide/topics/media-apps/audio-app/mediasession-callbacks">MediaSessionCallback</a>
 */
internal class MediaSessionCallback(
    private val serviceContext: Service,
    private val playbackNotificationBuilder: PlaybackNotificationBuilder,
    private val mediaSessionCompat: MediaSessionCompat,
    private val trackPlayer: TrackPlayer,
    private val audioManager: AudioManager,
    private val becomingNoisyReceiver: BecomingNoisyReceiver
) : MediaSessionCompat.Callback() {

    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onPlay() {
        super.onPlay()
        // Request audio focus for playback, this registers the afChangeListener
        // To output audio, it should request audio focus.Only one app can hold focus at a time
        val request = if (versionFrom(Build.VERSION_CODES.O)) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                .build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        if (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            initSession()
        }
    }

    private fun initSession() {
        with(serviceContext) {
            //This ensures that the service starts and continues to run, even when all UI MediaBrowser activities that are bound to it unbind.
            startService(Intent(this, ZikkMediaService::class.java))

            //TODO  Update metadata and state
            mediaSessionCompat.isActive = true

            trackPlayer.start()

            // Register BECOME_NOISY BroadcastReceiver
            registerReceiver(becomingNoisyReceiver, intentFilter)

            val notification = playbackNotificationBuilder.buildNotification()
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onPause() {
        super.onPause()
        with(serviceContext) {
            trackPlayer.pause()

            unregisterReceiver(becomingNoisyReceiver)

            // Take the service out of the foreground
            stopForeground(false)
        }
    }

    override fun onStop() {
        super.onStop()
        // Abandon audio focus
        if (versionFrom(Build.VERSION_CODES.O)) {
            audioFocusRequest?.run {
                audioManager.abandonAudioFocusRequest(this)
            }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        with(serviceContext) {
            // A started service must be explicitly stopped, whether or not it's bound. This ensures that your player continues to perform even if the controlling UI activity unbinds
            stopSelf()

            trackPlayer.stop()

            mediaSessionCompat.isActive = false
            stopForeground(false)
        }
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
    }

    companion object {
        private const val NOTIFICATION_ID = 1000
        private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val audioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener {
                //TODO Implement AudioFocus Listener
            }
    }
}