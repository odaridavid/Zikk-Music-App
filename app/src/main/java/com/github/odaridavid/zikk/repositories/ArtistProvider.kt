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
import android.provider.MediaStore
import com.github.odaridavid.zikk.models.Artist


internal class ArtistProvider(val applicationContext: Context) {

    /**
     * Returns a list of artists after extracting from a cursor
     */
    fun loadAllArtists(): List<Artist> {
        val artists = mutableListOf<Artist>()
        val cursor = getArtistCursor() ?: throw IllegalStateException("Artists cursor is null")
        while (cursor.moveToNext()) {
            artists.add(cursor.mapToArtistEntity())
        }
        cursor.close()
        return artists
    }

    private fun Cursor.mapToArtistEntity(): Artist {
        val artistId = getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
        val artistName = getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
        val noOfAlbums = getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
        val noOfTracks = getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
        return Artist(
            id = getLong(artistId),
            name = getString(artistName),
            noOfAlbums = getInt(noOfAlbums),
            noOfTracks = getInt(noOfTracks)
        )
    }

    private fun getArtistCursor(): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getArtistColumns()
        val sortOrder = "${MediaStore.Audio.Artists.ARTIST} DESC"
        return cr.query(uri, projection, null, null, sortOrder)
    }

    private fun getArtistColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )
    }

}