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
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.github.odaridavid.zikk.playback.BecomingNoisyReceiver
import com.github.odaridavid.zikk.playback.notification.PlaybackNotificationBuilder
import com.github.odaridavid.zikk.playback.session.MediaLoader
import com.github.odaridavid.zikk.repositories.*
import dagger.Module
import dagger.Provides

@Module
internal class PlaybackModule {

    @Provides
    fun providesMediaSessionCompat(
        context: Context
    ): MediaSessionCompat {
        return MediaSessionCompat(context, "Zikk Media Service").apply {

            setPlaybackState(
                Builder()
                    .setActions(
                        ACTION_PLAY or ACTION_PLAY_PAUSE or ACTION_PAUSE or ACTION_STOP or ACTION_SKIP_TO_NEXT
                    )
                    .build()
            )
            //TODO Set media session metadata
            //setMetadata()
        }
    }

    @Provides
    fun providesPlaybackNotificationBuilder(
        context: Context,
        mediaControllerCompat: MediaControllerCompat,
        mediaSessionCompat: MediaSessionCompat,
        notificationManager: NotificationManager
    ): PlaybackNotificationBuilder {
        return PlaybackNotificationBuilder(
            context,
            notificationManager,
            mediaControllerCompat,
            mediaSessionCompat
        )
    }

    @Provides
    fun providesBecomingNoisyReceiver(mediaControllerCompat: MediaControllerCompat): BecomingNoisyReceiver {
        return BecomingNoisyReceiver(
            mediaControllerCompat
        )
    }

    @Provides
    fun providesMediaControllerCompat(
        context: Context,
        mediaSessionCompat: MediaSessionCompat
    ): MediaControllerCompat {
        return MediaControllerCompat(context, mediaSessionCompat)
    }

    @Provides
    fun providesMediaLoader(
        context: Context,
        albumRepository: AlbumRepository,
        artistRepository: ArtistRepository,
        genreRepository: GenreRepository,
        trackRepository: TrackRepository,
        playlistRepository: PlaylistRepository
    ): MediaLoader {
        return MediaLoader(
            context,
            albumRepository,
            artistRepository,
            genreRepository,
            trackRepository,
            playlistRepository
        )
    }

}