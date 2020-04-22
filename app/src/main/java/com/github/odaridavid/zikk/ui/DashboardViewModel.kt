package com.github.odaridavid.zikk.ui

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
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class DashboardViewModel : ViewModel() {

    private val _nowPlaying = MutableLiveData<MediaMetadataCompat>()
    val nowPlaying: LiveData<MediaMetadataCompat>
        get() = _nowPlaying

    private val _playbackState = MutableLiveData<PlaybackStateCompat>()
    val playbackState: LiveData<PlaybackStateCompat>
        get() = _playbackState

    private val _isMediaBrowserConnected = MutableLiveData<Boolean>()
    val isMediaBrowserConnected: LiveData<Boolean>
        get() = _isMediaBrowserConnected

    init {
        //TODO Save last played track info and load on initialisation
        //Defaults
        _nowPlaying.value = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
            .build()

        _playbackState.value = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build()
    }

    fun setPlaybackState(state: PlaybackStateCompat) {
        _playbackState.value = state
    }

    fun setNowPlayingMetadata(mediaMetadataCompat: MediaMetadataCompat) {
        _nowPlaying.value = mediaMetadataCompat
    }

    fun setIsConnected(value: Boolean) {
        _isMediaBrowserConnected.value = value
    }

}

