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
import com.github.odaridavid.zikk.albums.AlbumModule
import com.github.odaridavid.zikk.artists.ArtistModule
import com.github.odaridavid.zikk.genres.GenreModule
import com.github.odaridavid.zikk.playback.PlaybackModule
import com.github.odaridavid.zikk.playback.ZikkMediaService
import com.github.odaridavid.zikk.playlists.PlaylistModule
import com.github.odaridavid.zikk.tracks.TrackModule
import com.github.odaridavid.zikk.ui.DashboardActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Application Dependency Graph
 */
@Singleton
@Component(
    modules = [
        AppModule::class,

        PlaybackModule::class,

        //Media Categories
        AlbumModule::class,
        ArtistModule::class,
        PlaylistModule::class,
        TrackModule::class,
        GenreModule::class
    ]
)
internal interface AppComponent {

    fun inject(zikkMediaService: ZikkMediaService)

    fun inject(dashboardActivity: DashboardActivity)

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): AppComponent

    }
}