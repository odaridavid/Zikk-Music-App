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
import com.github.odaridavid.zikk.models.Genre


internal class GenreRepository(val applicationContext: Context) {

    /**
     * Returns a list of genres after extracting from a cursor
     */
    fun loadAllGenres(): List<Genre> {
        val genres = mutableListOf<Genre>()
        val cursor = getGenreCursor() ?: throw IllegalStateException("Genres cursor is null")
        while (cursor.moveToNext()) {
            genres.add(cursor.mapToGenreEntity())
        }
        cursor.close()
        return genres
    }

    private fun Cursor.mapToGenreEntity(): Genre {
        val genreName = getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
        val genreId = getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
        return Genre(
            id = getLong(genreId),
            name = getString(genreName)
        )
    }

    private fun getGenreCursor(): Cursor? {
        val cr = applicationContext.contentResolver
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val projection: Array<String> = getGenreColumns()
        val sortOrder = "${MediaStore.Audio.Genres.NAME} DESC"
        return cr.query(uri, projection, null, null, sortOrder)
    }

    private fun getGenreColumns(): Array<String> {
        return arrayOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
        )
    }
}