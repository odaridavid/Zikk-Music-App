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
import android.content.Context
import com.github.odaridavid.zikk.playback.session.MediaLoader
import com.github.odaridavid.zikk.playback.player.TrackPlayer
import com.github.odaridavid.zikk.repositories.*
import dagger.Module
import dagger.Provides

@Module
internal class PlaybackModule {

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

    @Provides
    fun providesTrackPlayer(context: Context, trackRepository: TrackRepository): TrackPlayer {
        return TrackPlayer(
            context,
            trackRepository
        )
    }

}