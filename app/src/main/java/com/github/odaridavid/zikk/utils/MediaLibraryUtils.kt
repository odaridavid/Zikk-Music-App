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
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.StringRes
import com.github.odaridavid.zikk.playback.MediaItemId

fun createMediaItemsRootCategories(
    context: Context,
    @StringRes title: Int,
    @StringRes subtitle: Int,
    mediaItemId: MediaItemId
): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(mediaItemId.toString())
            .setTitle(context.getString(title))
            .setSubtitle(context.getString(subtitle))
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

//TODO Check on .setIconUri()
fun generateMediaItem(
    title: String,
    subtitle: String,
    mediaItemId: String,
    @MediaBrowserCompat.MediaItem.Flags mediaItemFlags: Int
): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(mediaItemId)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build(),
        mediaItemFlags
    )

}
