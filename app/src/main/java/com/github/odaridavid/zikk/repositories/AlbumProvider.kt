package com.github.odaridavid.zikk.repositories

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
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.github.odaridavid.zikk.models.Album


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
     * Returns a filtered list of albums based on the query after extracting from a cursor
     */
    fun loadAlbumsByQuery(selection: String, selectionArgs: Array<String>): List<Album> {
        val albums = mutableListOf<Album>()
        val cursor = getAlbumsCursor(selection, selectionArgs)
            ?: throw IllegalStateException("Albums cursor is null")
        if (cursor.count != 0) {
            cursor.moveToFirst()
            do {
                albums.add(cursor.mapToAlbumEntity())
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albums
    }

    /**
     * Convert cursor rows to an album entity
     */
    private fun Cursor.mapToAlbumEntity(): Album {
        val id = getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
        val albumTitle = getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
        val albumArtist = getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
        val noOfSongs = getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
        val albumArt = getAlbumArtPath(getLong(id))
        return Album(
            id = getLong(id),
            title = getString(albumTitle),
            artist = getString(albumArtist),
            noOfSongs = getInt(noOfSongs),
            albumArt = albumArt
        )
    }

    private fun getAlbumArtPath(id: Long): String {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        return Uri.withAppendedPath(artworkUri, "$id").toString()
    }

    /**
     * Loads information on albums using a content resolver and returns a cursor object
     */
    private fun getAlbumsCursor(
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getAlbumColumns()
        val sortOrder = "${MediaStore.Audio.Albums.LAST_YEAR} DESC"
        return cr.query(uri, projection, selection, selectionArgs, sortOrder)
    }

    private fun getAlbumColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
    }

}