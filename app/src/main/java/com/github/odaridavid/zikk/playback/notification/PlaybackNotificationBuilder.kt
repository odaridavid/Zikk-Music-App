package com.github.odaridavid.zikk.playback.notification

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
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.*
import com.github.odaridavid.zikk.utils.Constants.PLAYBACK_NOTIFICATION_ID
import javax.inject.Inject

/**
 * Responsible for playback notification.
 */
internal class PlaybackNotificationBuilder @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val mediaSessionCompat: MediaSessionCompat
) {

    fun build(): Notification {

        if (versionFrom(Build.VERSION_CODES.O) && !hasPlaybackChannel())
            createPlaybackNotificationChannel()

        val mediaMetadata = mediaSessionCompat.controller.metadata

        val notificationBuilder = NotificationCompat.Builder(context, PLAYBACK_CHANNEL_ID)
            .apply {
                // Add the metadata for the currently playing track
                setContentTitle(mediaMetadata.title)
                setContentText(mediaMetadata.artist)
                setSubText(mediaMetadata.album)
                setLargeIcon(mediaMetadata.albumArt)

                // Enable launching the player by clicking the notification
                setContentIntent(mediaSessionCompat.controller.sessionActivity)

                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        ACTION_STOP
                    )
                )

                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                //TODO Replace with app icon
                setSmallIcon(R.drawable.ic_music_note_black_24dp)
                color = ContextCompat.getColor(context, R.color.colorAccent)

                val playPauseRes =
                    if (mediaSessionCompat.controller.playbackState.state == STATE_PLAYING)
                        R.drawable.ic_pause_black_48dp
                    else R.drawable.ic_play_black_48dp

                addAction(
                    NotificationCompat.Action(
                        playPauseRes,
                        context.getString(R.string.playback_action_pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            ACTION_PLAY_PAUSE
                        )
                    )
                )
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_skip_next_black_48dp,
                        context.getString(R.string.playback_action_skip_to_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            ACTION_SKIP_TO_NEXT
                        )
                    )
                )

                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                ACTION_STOP
                            )
                        )
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                )
            }
        val notification = notificationBuilder.build()
        notificationManager.notify(PLAYBACK_NOTIFICATION_ID, notification)
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPlaybackNotificationChannel() {
        val name = context.getString(R.string.notification_playback_channel_name)
        val descriptionText =
            context.getString(R.string.notification_playback_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(PLAYBACK_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hasPlaybackChannel(): Boolean {
        return notificationManager.getNotificationChannel(PLAYBACK_CHANNEL_ID) != null
    }

    companion object {
        private const val PLAYBACK_CHANNEL_ID = "playback"
    }
}