package com.github.odaridavid.zikk.di

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
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import com.github.odaridavid.zikk.playback.notification.NotificationsChannelManager
import com.github.odaridavid.zikk.utils.Constants
import dagger.Module
import dagger.Provides

/**
 * Application wide dependencies
 */
@Module
class AppModule {

    @Provides
    fun providesNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun providesAudioManager(context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Provides
    fun providesSharedPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    fun providesNotificationChannelsManager(
        context: Context,
        notificationManager: NotificationManager
    ): NotificationsChannelManager {
        return NotificationsChannelManager(context, notificationManager)
    }
}