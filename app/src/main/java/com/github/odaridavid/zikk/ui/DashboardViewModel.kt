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
import timber.log.Timber

internal class DashboardViewModel : ViewModel() {

    private val _isMediaBrowserConnected = MutableLiveData<Boolean>()
    val isMediaBrowserConnected: LiveData<Boolean>
        get() = _isMediaBrowserConnected

    fun setIsConnected(value: Boolean) {
        _isMediaBrowserConnected.value = value
    }

    private val _nowPlayingStatus =
        MutableLiveData<Pair<Pair<Int, Boolean>, Pair<Int, Boolean>>>()
    val nowPlayingStatus: LiveData<Pair<Pair<Int, Boolean>, Pair<Int, Boolean>>>
        get() = _nowPlayingStatus

    fun setNowPlayingStatus(prevAndNextTrack: Pair<Pair<Int, Boolean>, Pair<Int, Boolean>>) {
        Timber.d("Value Set")
        _nowPlayingStatus.value = prevAndNextTrack
    }


}

