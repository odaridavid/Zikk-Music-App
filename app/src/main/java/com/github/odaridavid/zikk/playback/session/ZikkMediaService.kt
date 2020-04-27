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
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.github.odaridavid.zikk.data.LastPlayedTrackPreference
import com.github.odaridavid.zikk.models.MediaId
import com.github.odaridavid.zikk.notification.PlaybackNotificationBuilder
import com.github.odaridavid.zikk.playback.BecomingNoisyReceiver
import com.github.odaridavid.zikk.playback.player.TrackPlayer
import com.github.odaridavid.zikk.utils.injector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Media Browser Service which controls the media session and co-ordinates with the media
 * browser attached through the media player.
 * @see <a href="Media App Architecture">https://developer.android.com/guide/topics/media-apps/media-apps-overview</a>
 */
internal class ZikkMediaService : MediaBrowserServiceCompat(),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private val playbackStateCompatBuilder = PlaybackStateCompat.Builder()
    private var metadataCompatBuilder = MediaMetadataCompat.Builder()

    @Inject
    lateinit var mediaLoader: MediaLoader

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var trackPlayer: TrackPlayer

    @Inject
    lateinit var lastPlayedTrackPreference: LastPlayedTrackPreference

    override fun onCreate() {
        injector.inject(this)
        super.onCreate()
        mediaSessionCompat = createMediaSession()
        mediaSessionCompat.isActive = true

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
        mediaSessionCompat.setSessionActivity(sessionActivityPendingIntent)

        this.sessionToken = mediaSessionCompat.sessionToken

        val playbackNotificationBuilder = PlaybackNotificationBuilder(
            this,
            notificationManager,
            mediaSessionCompat
        )

        becomingNoisyReceiver = BecomingNoisyReceiver(mediaSessionCompat.controller)

        mediaSessionCompat.setCallback(
            MediaSessionCallback(
                this,
                playbackNotificationBuilder,
                mediaSessionCompat,
                audioManager,
                becomingNoisyReceiver,
                playbackStateCompatBuilder,
                metadataCompatBuilder,
                trackPlayer,
                lastPlayedTrackPreference
            )
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopForeground(true)
            return START_STICKY
        }
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        return START_STICKY
    }

    /**
     * Called to get information about the children nodes of a media item when subscribed to.
     *
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        //  Browsing not allowed
        if (EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        if (MEDIA_ROOT_ID == parentId) {
            //if this is the root menu build the MediaItem objects for the top level as browsable roots
            mediaLoader.buildMediaCategories(mediaItems)
            result.sendResult(mediaItems)
        } else {
            result.detach()
            launch {
                val mediaItemId = MediaId.values().find { id -> id.toString() == parentId }
                mediaLoader.getMediaItemChildren(mediaItems, mediaItemId)
                result.sendResult(mediaItems)
            }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //Restrict Browsing Access to current app
        return if (clientPackageName == packageName)
            BrowserRoot(MEDIA_ROOT_ID, null)
        else BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionCompat.isActive = false
        trackPlayer.release()
        mediaSessionCompat.release()
    }

    private fun createMediaSession(): MediaSessionCompat {
        return MediaSessionCompat(this, "Zikk Media Service").apply {
            setPlaybackState(createPlaybackState())
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
        }
    }

    private fun createPlaybackState(): PlaybackStateCompat {
        return playbackStateCompatBuilder
            .setActions(
                ACTION_PLAY or ACTION_PLAY_PAUSE or ACTION_PAUSE or ACTION_STOP
            )
            .build()

    }

    companion object {
        private const val MEDIA_ROOT_ID = "root"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root"
    }
}
