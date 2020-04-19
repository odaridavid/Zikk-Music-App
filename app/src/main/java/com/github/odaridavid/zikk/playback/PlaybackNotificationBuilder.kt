package com.github.odaridavid.zikk.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.versionFrom
import javax.inject.Inject

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
internal class PlaybackNotificationBuilder @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val mediaControllerCompat: MediaControllerCompat,
    private val mediaSessionCompat: MediaSessionCompat
) {

    /**
     * Responsible for creating the media playback notification
     */
    fun buildNotification() {
        createPlaybackNotificationChannel()

        val mediaMetadata = mediaControllerCompat.metadata
        val description = mediaMetadata.description

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .apply {
                // Add the metadata for the currently playing track
                setContentTitle(description.title)
                setContentText(description.subtitle)
                setSubText(description.description)
                setLargeIcon(description.iconBitmap)

                // Enable launching the player by clicking the notification
                setContentIntent(mediaControllerCompat.sessionActivity)

                // Stop the service when the notification is swiped away,media sesion callback on stop triggered,Avail API 21 >
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                //TODO Replace with app icon
                setSmallIcon(R.drawable.ic_music_note_black_24dp)
                color = ContextCompat.getColor(context, R.color.colorAccent)

                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_pause_black_48dp,
                        context.getString(R.string.playback_action_pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                //Use Notification Media Style
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0)
                )
            }
        with(notificationManager) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }

    }

    private fun createPlaybackNotificationChannel() {
        if (versionFrom(Build.VERSION_CODES.O)) {
            val name = context.getString(R.string.notification_playback_channel_name)
            val descriptionText =
                context.getString(R.string.notification_playback_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1000
    }
}