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
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.odaridavid.zikk.playback.BecomingNoisyReceiver
import com.github.odaridavid.zikk.playback.MediaId
import com.github.odaridavid.zikk.playback.notification.PlaybackNotificationBuilder
import com.github.odaridavid.zikk.repositories.TrackRepository
import com.github.odaridavid.zikk.utils.injector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Media Browser Service which controls the media session and co-ordinates with the media
 * browser attached through the media player.
 * @see <a href="Media App Architecture">https://developer.android.com/guide/topics/media-apps/media-apps-overview</a>
 */
internal class ZikkMediaService : MediaBrowserServiceCompat() {
    //TODO Optimize for android auto and wearables
    @Inject
    lateinit var mediaSessionCompat: MediaSessionCompat

    @Inject
    lateinit var mediaLoader: MediaLoader

    lateinit var playbackNotificationBuilder: PlaybackNotificationBuilder

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    @Inject
    lateinit var trackPlayer: TrackPlayer

    @Inject
    lateinit var trackRepository: TrackRepository

    override fun onCreate() {
        injector.inject(this)
        super.onCreate()

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
        mediaSessionCompat.setSessionActivity(sessionActivityPendingIntent)

        this.sessionToken = mediaSessionCompat.sessionToken

        playbackNotificationBuilder = PlaybackNotificationBuilder(
            this,
            notificationManager,
            mediaSessionCompat
        )

        mediaSessionCompat.setCallback(
            MediaSessionCallback(
                this,
                playbackNotificationBuilder,
                mediaSessionCompat,
                audioManager,
                becomingNoisyReceiver,
                trackPlayer,
                trackRepository
            )
        )
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
            //TODO Create coroutine scope that is cancelable and launch work in scope
            GlobalScope.launch(Dispatchers.IO) {
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
        trackPlayer.release()
        mediaSessionCompat.release()
    }

    companion object {
        private const val MEDIA_ROOT_ID = "root"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root"

        //Player Actions
        private const val ACTION_PLAY: String = "com.github.odaridavid.zikk.playback.session.PLAY"
        private const val ACTION_PAUSE: String = "com.github.odaridavid.zikk.playback.session.PAUSE"
        private const val ACTION_STOP: String = "com.github.odaridavid.zikk.playback.session.STOP"
        private const val ACTION_SKIP: String = "com.github.odaridavid.zikk.playback.session.SKIP"
    }
}