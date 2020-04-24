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
import com.github.odaridavid.zikk.data.ShowPlayerPreference
import com.github.odaridavid.zikk.models.PlaybackStatus

internal class DashboardViewModel(private val showPlayerPreference: ShowPlayerPreference) :
    ViewModel() {

    private val _isMediaBrowserConnected = MutableLiveData<Boolean>()
    val isMediaBrowserConnected: LiveData<Boolean>
        get() = _isMediaBrowserConnected

    private val _playbackStatus = MutableLiveData<PlaybackStatus>()
    val playbackStatus: LiveData<PlaybackStatus>
        get() = _playbackStatus

    private val _playerActive = MutableLiveData<Boolean>()
    val playerActive: LiveData<Boolean>
        get() = _playerActive

    init {
        _playerActive.value = showPlayerPreference.hasPlayedTrackBefore()
    }

    fun setIsConnected(value: Boolean) {
        _isMediaBrowserConnected.value = value
    }

    fun setNowPlayingStatus(playbackStatus: PlaybackStatus) {
        _playbackStatus.value = playbackStatus
    }

    fun setPlayerActive() {
        showPlayerPreference.setHasPlayedTrackBefore()
        checkPlayerStatus()
    }

    fun checkPlayerStatus() {
        _playerActive.value = showPlayerPreference.hasPlayedTrackBefore()
    }

    class Factory(
        private val showPlayerPreference: ShowPlayerPreference
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DashboardViewModel(showPlayerPreference) as T
        }
    }
}

