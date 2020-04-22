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
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat

fun createMediaItem(
    title: String,
    subtitle: String,
    mediaItemId: String,
    iconUri: Uri?,
    description: String,
    @MediaBrowserCompat.MediaItem.Flags mediaItemFlags: Int
): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(mediaItemId)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setIconUri(iconUri)
            .setMediaUri(null)
            .setDescription(description)
            .build(),
        mediaItemFlags
    )
}

fun getDrawableUri(context: Context, drawableName: String): Uri {
    val appPackageName = context.packageName
    return Uri.parse("android.resource://$appPackageName/drawable/$drawableName")
}
