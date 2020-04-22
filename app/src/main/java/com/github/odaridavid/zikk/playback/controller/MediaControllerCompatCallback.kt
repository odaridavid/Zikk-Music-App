package com.github.odaridavid.zikk.playback.controller

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
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import timber.log.Timber

/**
 * Media controller callback receives updates of state changes from the active media session
 */
class MediaControllerCompatCallback : MediaControllerCompat.Callback() {

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(state)
        Timber.d("Playback State Changed:$state")
        //TODO Update on playback state changed
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        super.onMetadataChanged(metadata)
        Timber.d("Metadata Changed $metadata")
        //TODO Update on metadata changed
    }
}