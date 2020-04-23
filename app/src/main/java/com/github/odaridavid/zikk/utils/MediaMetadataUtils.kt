package com.github.odaridavid.zikk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.github.odaridavid.zikk.R
import java.io.FileNotFoundException

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
inline val MediaMetadataCompat.id: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline val MediaMetadataCompat.artist: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

inline val MediaMetadataCompat.duration
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

inline val MediaMetadataCompat.album: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

inline val MediaMetadataCompat.albumArtUri: Uri
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI).toUri()

inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)


fun getAlbumArtBitmap(context: Context, imageUri: Uri): Bitmap? {
    val cr = context.contentResolver
    return try {
        if (versionFrom(Build.VERSION_CODES.P)) {
            val src = ImageDecoder.createSource(cr, imageUri)
            ImageDecoder.decodeBitmap(src)
        } else MediaStore.Images.Media.getBitmap(cr, imageUri)
    } catch (e: FileNotFoundException) {
        BitmapFactory.decodeResource(context.resources, R.drawable.bg_no_art)
    }
}

