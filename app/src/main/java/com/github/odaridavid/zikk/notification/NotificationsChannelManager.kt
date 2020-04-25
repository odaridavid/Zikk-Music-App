package com.github.odaridavid.zikk.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.odaridavid.zikk.R
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
class NotificationsChannelManager @Inject constructor(
    val context: Context,
    private val notificationManager: NotificationManager
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(channelId: String) {
        val channel = when (channelId) {
            PLAYBACK_CHANNEL_ID -> {
                val name = context.getString(R.string.notification_playback_channel_name)
                val descriptionText =
                    context.getString(R.string.notification_playback_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                NotificationChannel(channelId, name, importance).apply {
                    description = descriptionText
                }
            }
            else -> throw IllegalArgumentException("Unknown Channel Id Received")
        }
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasChannel(channelId: String): Boolean {
        return notificationManager.getNotificationChannel(channelId) != null
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "playback"
    }
}