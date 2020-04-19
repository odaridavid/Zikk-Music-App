package com.github.odaridavid.zikk.playback

import android.app.NotificationManager
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dagger.Module
import dagger.Provides

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
@Module
internal class PlaybackModule {

    @Provides
    fun providesMediaSessionCompat(
        context: Context,
        playbackState: PlaybackStateCompat,
        callback: MediaSessionCompat.Callback
    ): MediaSessionCompat {
        return MediaSessionCompat(context, "Zikk Media Service").apply {
            setPlaybackState(playbackState)
        }
    }

    @Provides
    fun providesPlaybackState(): PlaybackStateCompat {
        return PlaybackStateBuilder.instance
    }

    @Provides
    fun providesPlaybackNotificationBuilder(
        context: Context,
        mediaControllerCompat: MediaControllerCompat,
        mediaSessionCompat: MediaSessionCompat,
        notificationManager: NotificationManager
    ): PlaybackNotificationBuilder {
        return PlaybackNotificationBuilder(context, notificationManager, mediaControllerCompat,mediaSessionCompat)
    }

    @Provides
    fun providesMediaControllerCompat(mediaSessionCompat: MediaSessionCompat): MediaControllerCompat {
        return mediaSessionCompat.controller
    }
}