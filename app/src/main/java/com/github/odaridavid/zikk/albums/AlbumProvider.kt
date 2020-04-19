package com.github.odaridavid.zikk.albums

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

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
internal class AlbumProvider(private val applicationContext: Context) {

    /**
     * Returns a list of albums after extracting from a cursor
     */
    fun loadAllAlbums(): List<Album> {
        val albums = mutableListOf<Album>()
        val cursor = getAlbumsCursor() ?: throw IllegalStateException("Albums cursor is null")
        while (cursor.moveToNext()) {
            albums.add(cursor.mapToAlbumEntity())
        }
        cursor.close()
        return albums
    }

    /**
     * Convert cursor rows to an album entity
     */
    private fun Cursor.mapToAlbumEntity(): Album {
        val albumId = getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID)
        val albumTitle = getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
        val albumArtist = getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
        val noOfSongs = getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
        val artistId = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
        val modificationYear = getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR)
        return Album(
            id = getLong(albumId),
            title = getString(albumTitle),
            artist = getString(albumArtist),
            noOfSongs = getInt(noOfSongs),
            artistId = getLong(artistId),
            latestYear = getString(modificationYear)
        )
    }

    /**
     * Loads information on albums using a content resolver and returns a cursor object
     */
    private fun getAlbumsCursor(): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getAlbumColumns()
        val sortOrder = "${MediaStore.Audio.Albums.LAST_YEAR} DESC"
        return cr.query(uri, projection, null, null, sortOrder)
    }

    //TODO Check on deprecated album art column alternative referencing https://developer.android.com/reference/android/content/ContentResolver#loadThumbnail(android.net.Uri,%20android.util.Size,%20android.os.CancellationSignal)
    private fun getAlbumColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.LAST_YEAR,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Media.ARTIST_ID //Albums Artist Id Constant avail on Q >
        )
    }

}