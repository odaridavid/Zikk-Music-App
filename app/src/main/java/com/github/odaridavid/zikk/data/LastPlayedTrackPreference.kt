package com.github.odaridavid.zikk.data

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
import android.content.SharedPreferences
import javax.inject.Inject

/**
 * Used to show or hide the player if no track has ever been played since install
 */
internal class LastPlayedTrackPreference @Inject constructor(private val sharedPreferences: SharedPreferences) {

    var lastPlayedTrackId: Long
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putLong(KEY_LAST_PLAYED_TRACK_ID, value)
            editor.apply()
        }
        get() {
            return sharedPreferences.getLong(KEY_LAST_PLAYED_TRACK_ID, DEFAULT_VALUE)
        }

    companion object {
        private const val KEY_LAST_PLAYED_TRACK_ID = "last_played_id"
        private const val DEFAULT_VALUE = -1L
    }
}