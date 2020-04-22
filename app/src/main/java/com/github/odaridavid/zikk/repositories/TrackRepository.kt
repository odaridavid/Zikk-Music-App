package com.github.odaridavid.zikk.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.github.odaridavid.zikk.models.Track

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
internal class TrackRepository(
    val applicationContext: Context,
    private val albumRepository: AlbumRepository
) {
    /**
     * Returns a list of tracks after extracting from a cursor
     */
    fun loadAllTracks(): List<Track> {
        val tracks = mutableListOf<Track>()
        val cursor = getTrackCursor() ?: throw IllegalStateException("Tracks cursor is null")
        while (cursor.moveToNext()) {
            tracks.add(cursor.mapToTrackEntity())
        }
        cursor.close()
        return tracks
    }

    private fun Cursor.mapToTrackEntity(): Track {
        val trackId = getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val artistId = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
        val artistName = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumName = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val title = getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val displayName = getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val track = getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
        val duration = getColumnIndexOrThrow(MEDIA_STORE_AUDIO_DURATION_COLUMN)
        val fileUri =
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getString(trackId))
        val albumArt = getAlbumArt(getString(albumName))
        return Track(
            id = getLong(trackId),
            artistId = getLong(artistId),
            artist = getString(artistName),
            album = getString(albumName),
            title = getString(title),
            displayName = getString(displayName),
            track = getString(track),
            duration = getString(duration),
            filePath = fileUri.toString(),
            albumArt = albumArt
        )
    }

    private fun getAlbumArt(album: String): String {
        val albums = albumRepository.loadAlbumsByQuery("album=?", arrayOf(album))
        return if (albums.isEmpty()) "" else albums[0].albumArt
    }

    private fun getTrackCursor(): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getTrackColumns()
        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        val selection = "is_music=?" //Selects only music,leaves out media such as notifications
        val selectionArgs = arrayOf("1")
        return cr.query(uri, projection, selection, selectionArgs, sortOrder)
    }

    private fun getTrackColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TRACK,
            MEDIA_STORE_AUDIO_DURATION_COLUMN
        )
    }

    companion object {
        private const val MEDIA_STORE_AUDIO_DURATION_COLUMN = "duration"
    }
}