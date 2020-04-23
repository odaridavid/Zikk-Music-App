package com.github.odaridavid.zikk.playback.session

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.models.MediaCategoryInfo
import com.github.odaridavid.zikk.models.MediaId
import com.github.odaridavid.zikk.repositories.*
import com.github.odaridavid.zikk.utils.convertMillisecondsToDuration
import com.github.odaridavid.zikk.utils.createMediaItem
import com.github.odaridavid.zikk.utils.getDrawableUri

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
internal class MediaLoader(
    private val context: Context,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository
) {

    fun getMediaItemChildren(
        mediaItems: MutableList<MediaBrowserCompat.MediaItem>,
        mediaId: MediaId?
    ) {
        if (mediaId != null)
            when (mediaId) {
                MediaId.ALBUM -> {
                    val albums = albumRepository.loadAllAlbums()
                    albums.forEach { album ->
                        mediaItems.add(
                            createMediaItem(
                                album.title,
                                context.resources.getQuantityString(
                                    R.plurals.number_of_songs,
                                    album.noOfSongs,
                                    album.noOfSongs
                                ),
                                "${MediaId.ALBUM}-${album.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.ARTIST -> {
                    val artists = artistRepository.loadAllArtists()
                    artists.forEach { artist ->
                        mediaItems.add(
                            createMediaItem(
                                artist.name,
                                context.resources.getQuantityString(
                                    R.plurals.number_of_albums,
                                    artist.noOfAlbums,
                                    artist.noOfAlbums
                                ),
                                "${MediaId.ARTIST}-${artist.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.GENRE -> {
                    val genres = genreRepository.loadAllGenres()
                    genres.forEach { genre ->
                        mediaItems.add(
                            createMediaItem(
                                genre.name,
                                "",
                                "${MediaId.GENRE}-${genre.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )

                    }
                }
                MediaId.PLAYLIST -> {
                    val playlists = playlistRepository.loadAllPlaylists()
                    playlists.forEach { playlist ->
                        mediaItems.add(
                            createMediaItem(
                                playlist.name,
                                playlist.modified,
                                "${MediaId.PLAYLIST}-${playlist.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.TRACK -> {
                    val tracks = trackRepository.loadAllTracks()
                    tracks.forEach { track ->
                        mediaItems.add(
                            createMediaItem(
                                track.title,
                                track.artist,
                                "${MediaId.TRACK}-${track.id}",
                                Uri.parse(track.albumArt),
                                convertMillisecondsToDuration(track.duration.toLong()),
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                            )
                        )
                    }
                }
            }
    }


    fun buildMediaCategories(mediaItems: MutableList<MediaBrowserCompat.MediaItem>) {
        with(context) {
            val mediaCategoriesInfo =
                mutableListOf(
                    MediaCategoryInfo(
                        MediaId.TRACK,
                        getString(R.string.title_tracks),
                        getString(R.string.subtitle_all_tracks),
                        getDrawableUri(this, "ic_tracks_black_24dp"),
                        "",
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    ),
                    MediaCategoryInfo(
                        MediaId.ALBUM,
                        getString(R.string.title_albums),
                        getString(R.string.subtitle_all_albums),
                        getDrawableUri(this, "ic_album_black_24dp"),
                        "",
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                    ),
                    MediaCategoryInfo(
                        MediaId.ARTIST,
                        getString(R.string.title_artists),
                        getString(R.string.subtitle_all_artists),
                        getDrawableUri(this, "ic_artist_black_24dp"),
                        "",
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                    ),
                    MediaCategoryInfo(
                        MediaId.PLAYLIST,
                        getString(R.string.title_playlists),
                        getString(R.string.subtitle_all_playlists),
                        getDrawableUri(this, "ic_playlist_black_24dp"),
                        "",
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                    ),
                    MediaCategoryInfo(
                        MediaId.GENRE,
                        getString(R.string.title_genres),
                        getString(R.string.subtitle_all_genres),
                        getDrawableUri(this, "ic_genres_black_24dp"),
                        "",
                        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                    )
                )

            for (mediaCategoryInfo in mediaCategoriesInfo) {
                mediaItems.add(
                    createMediaItem(
                        mediaCategoryInfo.title,
                        mediaCategoryInfo.subtitle,
                        mediaCategoryInfo.id.toString(),
                        mediaCategoryInfo.iconUri,
                        mediaCategoryInfo.description,
                        mediaCategoryInfo.mediaFlags
                    )
                )
            }
        }
    }
}