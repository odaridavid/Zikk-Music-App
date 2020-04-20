package com.github.odaridavid.zikk.playback

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
import android.support.v4.media.MediaBrowserCompat
import timber.log.Timber

class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

    override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
    ) {
        super.onChildrenLoaded(parentId, children)
        Timber.d("$parentId Loaded")
    }

    override fun onError(parentId: String) {
        super.onError(parentId)
        Timber.d("Error on $parentId")
    }
}