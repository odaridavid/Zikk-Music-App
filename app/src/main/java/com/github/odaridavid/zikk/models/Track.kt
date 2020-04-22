package com.github.odaridavid.zikk.models

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
internal data class Track(
    val id: Long,
    val artistId: Long,
    val albumId: Long,
    val title: String,
    val album: String,
    val artist: String,
    val displayName: String,
    val track: String,
    val duration: String,
    val filePath: String,
    val albumArt: String
)