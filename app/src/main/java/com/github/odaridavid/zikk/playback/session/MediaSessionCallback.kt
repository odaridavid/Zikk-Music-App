package com.github.odaridavid.zikk.playback.session

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
import android.content.ContentUris
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.net.toUri
import com.github.odaridavid.zikk.models.Track
import com.github.odaridavid.zikk.playback.BecomingNoisyReceiver
import com.github.odaridavid.zikk.playback.notification.PlaybackNotificationBuilder
import com.github.odaridavid.zikk.repositories.TrackRepository
import com.github.odaridavid.zikk.utils.Constants.PLAYBACK_NOTIFICATION_ID
import com.github.odaridavid.zikk.utils.getAlbumArtBitmap
import com.github.odaridavid.zikk.utils.versionFrom
import timber.log.Timber

/**
 * MediaSessionCallback receives updates from initiated media controller actions
 *
 * @see <a href="https://developer.android.com/guide/topics/media-apps/audio-app/mediasession-callbacks">MediaSessionCallback</a>
 */
internal class MediaSessionCallback(
    private val serviceContext: Service,
    private val playbackNotificationBuilder: PlaybackNotificationBuilder,
    private val mediaSessionCompat: MediaSessionCompat,
    private val audioManager: AudioManager,
    private val becomingNoisyReceiver: BecomingNoisyReceiver,
    private val trackPlayer: TrackPlayer,
    private val trackRepository: TrackRepository
) : MediaSessionCompat.Callback(), AudioManager.OnAudioFocusChangeListener {

    //TODO Cleanup
    private var audioFocusRequest: AudioFocusRequest? = null
    private var metadataCompatBuilder = Builder()

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        val request = initAudioFocus()

        if (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            initSession(mediaId)
        }
    }

    override fun onPlay() {
        super.onPlay()
        // Request audio focus for playback, this registers the afChangeListener
        // To output audio, it should request audio focus.Only one app can hold focus at a time
        val request = initAudioFocus()

        if (request == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            initSession()
        }
    }

    private fun initAudioFocus(): Int {
        return if (versionFrom(Build.VERSION_CODES.O)) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                .build()
            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            audioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun initSession(mediaId: String? = null) {
        with(serviceContext) {
            //This ensures that the service starts and continues to run, even when all UI MediaBrowser activities that are bound to it unbind.
            startService(Intent(this, ZikkMediaService::class.java))

            mediaSessionCompat.isActive = true
            val trackId = convertMediaIdToFileId(mediaId)
            if (trackId == 0L) return
            val contentUris =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
            val track = trackRepository.loadTrackForId(trackId.toString()) ?: return
            Timber.d("$track")
            with(trackPlayer) {
                reset()
                setDataSource(serviceContext, contentUris)
                prepare()
            }

            setMetadata(track)

            // Register BECOME_NOISY BroadcastReceiver
            registerReceiver(
                becomingNoisyReceiver,
                intentFilter
            )

            val notification = playbackNotificationBuilder.buildNotification()
            startForeground(PLAYBACK_NOTIFICATION_ID, notification)
        }
    }

    private fun setMetadata(track: Track) {

        val trackMetadata = metadataCompatBuilder.apply {
            putString(METADATA_KEY_ALBUM, track.album)
            putString(METADATA_KEY_ARTIST, track.artist)
            putString(METADATA_KEY_TITLE, track.title)
            putString(METADATA_KEY_ALBUM_ART_URI, track.albumArt)
            putBitmap(
                METADATA_KEY_ALBUM_ART,
                getAlbumArtBitmap(serviceContext, track.albumArt.toUri())
            )
            putString(METADATA_KEY_MEDIA_ID, track.id.toString())
            putLong(METADATA_KEY_DURATION, track.duration.toLong())
        }
        mediaSessionCompat.setMetadata(trackMetadata.build())
    }

    private fun convertMediaIdToFileId(mediaId: String?): Long {
        val spIndex = mediaId?.indexOf('-') ?: -1
        return mediaId?.substring(spIndex + 1)!!.toLong()
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
        releaseAudioFocus()

        with(serviceContext) {
            // A started service must be explicitly stopped, whether or not it's bound. This ensures that your player continues to perform even if the controlling UI activity unbinds
            stopSelf()

            trackPlayer.stop()

            mediaSessionCompat.isActive = false
            stopForeground(false)
        }
    }

    private fun releaseAudioFocus() {
        if (versionFrom(Build.VERSION_CODES.O)) {
            audioFocusRequest?.run {
                audioManager.abandonAudioFocusRequest(this)
            }
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        //TODO Skip to next implementation
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                //TODO Switch Implementation to Reduce Volume
                trackPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                releaseAudioFocus()
                trackPlayer.stop()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                //TODO Switch Implementation to Increase volume
                trackPlayer.start()
            }
        }
    }

    companion object {
        private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }


}