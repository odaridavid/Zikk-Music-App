package com.github.odaridavid.zikk.playlists

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

internal class PlaylistProvider(val applicationContext: Context) {

    /**
     * Returns a list of playlists after extracting from a cursor
     */
    fun loadAllPlaylists(): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        val cursor = getPlaylistCursor() ?: throw IllegalStateException("Playlists cursor is null")
        while (cursor.moveToNext()) {
            playlists.add(cursor.mapToPlaylistEntity())
        }
        cursor.close()
        return playlists
    }

    private fun Cursor.mapToPlaylistEntity(): Playlist {
        val playlistId = getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID)
        val playlistName = getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME)
        val playlistLastModifiedDate =
            getColumnIndexOrThrow(MediaStore.Audio.Playlists.DATE_MODIFIED)
        val noOfTracks = getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members._COUNT)
        return Playlist(
            id = getLong(playlistId),
            name = getString(playlistName),
            modified = getString(playlistLastModifiedDate),
            noOfTracks = getInt(noOfTracks)
        )
    }

    private fun getPlaylistCursor(): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getPlaylistColumns()
        val sortOrder = "${MediaStore.Audio.Playlists.NAME} DESC"
        return cr.query(uri, projection, null, null, sortOrder)
    }

    private fun getPlaylistColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME,
            MediaStore.Audio.Playlists.DATE_MODIFIED,
            MediaStore.Audio.Playlists.Members._COUNT
        )
    }
}