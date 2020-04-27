package com.github.odaridavid.zikk.utils

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
import android.support.v4.media.session.PlaybackStateCompat

//TODO Move to a debug source set
internal object DebugUtils {
    fun getPlaybackState(state: Int): String {
        return when (state) {
            PlaybackStateCompat.STATE_NONE -> "Playback None"
            PlaybackStateCompat.STATE_STOPPED -> "Playback Stopped"
            PlaybackStateCompat.STATE_ERROR -> "Playback Error"
            PlaybackStateCompat.STATE_PLAYING -> "Playback Playing"
            PlaybackStateCompat.STATE_PAUSED -> "Playback Paused"
            PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> "Playback Skipping To Next"
            PlaybackStateCompat.STATE_BUFFERING -> "Playback Buffering"
            else -> "Unknown Playback State"
        }
    }
}