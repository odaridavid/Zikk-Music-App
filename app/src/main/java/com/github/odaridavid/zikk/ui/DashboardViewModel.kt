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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.odaridavid.zikk.data.LastPlayedTrackPreference
import com.github.odaridavid.zikk.models.PlaybackStatus

internal class DashboardViewModel(private val lastPlayedTrackPreference: LastPlayedTrackPreference) :
    ViewModel() {

    private val _isMediaBrowserConnected = MutableLiveData<Boolean>()
    val isMediaBrowserConnected: LiveData<Boolean>
        get() = _isMediaBrowserConnected

    private val _playbackStatus = MutableLiveData<PlaybackStatus>()
    val playbackStatus: LiveData<PlaybackStatus>
        get() = _playbackStatus

    private val _lastPlayedTrackId = MutableLiveData<Long>()
    val lastPlayedTrackId: LiveData<Long>
        get() = _lastPlayedTrackId

    init {
        _lastPlayedTrackId.value = lastPlayedTrackPreference.getLastPlayedTrackId()
    }

    fun setIsConnected(value: Boolean) {
        _isMediaBrowserConnected.value = value
    }

    fun setNowPlayingStatus(playbackStatus: PlaybackStatus) {
        _playbackStatus.value = playbackStatus
    }

    fun setCurrentlyPlayingTrackId(trackId: Long) {
        lastPlayedTrackPreference.setLastPlayedTrackId(trackId)
    }

    fun checkLastPlayedTrackId() {
        _lastPlayedTrackId.value = lastPlayedTrackPreference.getLastPlayedTrackId()
    }

    class Factory(
        private val lastPlayedTrackPreference: LastPlayedTrackPreference
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DashboardViewModel(lastPlayedTrackPreference) as T
        }
    }
}

