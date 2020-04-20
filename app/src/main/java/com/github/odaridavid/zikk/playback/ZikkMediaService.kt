package com.github.odaridavid.zikk.playback

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
import android.app.PendingIntent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.albums.AlbumProvider
import com.github.odaridavid.zikk.artists.ArtistProvider
import com.github.odaridavid.zikk.genres.GenreProvider
import com.github.odaridavid.zikk.playlists.PlaylistProvider
import com.github.odaridavid.zikk.tracks.TrackProvider
import com.github.odaridavid.zikk.utils.createMediaItemsRootCategories
import com.github.odaridavid.zikk.utils.generateMediaItem
import com.github.odaridavid.zikk.utils.injector
import javax.inject.Inject


/**
 * Media Browser Service which controls the media session and co-ordinates with the media
 * browser attached
 * @see <a href="Media App Architecture">https://developer.android.com/guide/topics/media-apps/media-apps-overview</a>
 */
internal class ZikkMediaService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var mediaSessionCompat: MediaSessionCompat

    @Inject
    lateinit var albumProvider: AlbumProvider

    @Inject
    lateinit var artistProvider: ArtistProvider

    @Inject
    lateinit var playlistProvider: PlaylistProvider

    @Inject
    lateinit var genreProvider: GenreProvider

    @Inject
    lateinit var trackProvider: TrackProvider

    @Inject
    lateinit var playbackNotificationBuilder: PlaybackNotificationBuilder

    @Inject
    lateinit var trackPlayer: TrackPlayer

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    override fun onCreate() {
        injector.inject(this)
        super.onCreate()

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
        mediaSessionCompat.setSessionActivity(sessionActivityPendingIntent)


        this.sessionToken = mediaSessionCompat.sessionToken

        mediaSessionCompat.setCallback(
            MediaSessionCallback(
                this,
                playbackNotificationBuilder,
                mediaSessionCompat,
                trackPlayer,
                audioManager,
                becomingNoisyReceiver
            )
        )

    }

    /**
     * Called to get information about the children nodes of a media item when subscribed to.
     *
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        //  Browsing not allowed
        if (EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        if (MEDIA_ROOT_ID == parentId) {
            //if this is the root menu build the MediaItem objects for the top level as browsable roots
            buildMediaRoots(mediaItems)
        } else {
            //TODO Check on result.detach()
            val mediaItemId = MediaItemId.values().find { id -> id.toString() == parentId }
            getChildren(mediaItems, mediaItemId)
        }
        result.sendResult(mediaItems)
    }

    // Examine the passed parentId to see which submenu we're at,and put the children of that menu in the mediaItems list
    private fun getChildren(
        mediaItems: MutableList<MediaBrowserCompat.MediaItem>,
        mediaItemId: MediaItemId?
    ) {
        if (mediaItemId != null)
            when (mediaItemId) {
                MediaItemId.ALBUM -> {
                    val albums = albumProvider.loadAllAlbums()
                    albums.forEach { album ->
                        mediaItems.add(
                            generateMediaItem(
                                album.title,
                                resources.getQuantityString(
                                    R.plurals.number_of_songs,
                                    album.noOfSongs
                                ),
                                "${MediaItemId.ALBUM}${album.id}",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaItemId.ARTIST -> {
                    val artists = artistProvider.loadAllArtists()
                    artists.forEach { artist ->
                        mediaItems.add(
                            generateMediaItem(
                                artist.name,
                                resources.getQuantityString(
                                    R.plurals.number_of_albums,
                                    artist.noOfAlbums
                                ), "${MediaItemId.ARTIST}${artist.id}",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaItemId.GENRE -> {
                    val genres = genreProvider.loadAllGenres()
                    genres.forEach { genre ->
                        mediaItems.add(
                            generateMediaItem(
                                genre.name,
                                resources.getQuantityString(
                                    R.plurals.number_of_songs,
                                    genre.noOfTracks
                                ),
                                "${MediaItemId.GENRE}${genre.id}",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )

                    }
                }
                MediaItemId.PLAYLIST -> {
                    val playlists = playlistProvider.loadAllPlaylists()
                    playlists.forEach { playlist ->
                        mediaItems.add(
                            generateMediaItem(
                                playlist.name,
                                resources.getQuantityString(
                                    R.plurals.number_of_songs,
                                    playlist.noOfTracks
                                ),
                                "${MediaItemId.PLAYLIST}${playlist.id}",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )

                    }
                }
                MediaItemId.TRACK -> {
                    val tracks = trackProvider.loadAllTracks()
                    tracks.forEach { track ->
                        mediaItems.add(
                            generateMediaItem(
                                track.title,
                                track.artist,
                                "${MediaItemId.TRACK}${track.id}",
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                            )
                        )
                    }
                }
            }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //Restrict Browsing Access to current app
        return if (clientPackageName == packageName)
            BrowserRoot(MEDIA_ROOT_ID, null)
        else BrowserRoot(EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionCompat.release()
    }

    private fun buildMediaRoots(mediaItems: MutableList<MediaBrowserCompat.MediaItem>) {
        val mediaItemsInfo =
            mutableListOf(
                MediaItemInfo(R.string.title_tracks, MediaItemId.TRACK, R.string.title_tracks),
                MediaItemInfo(R.string.title_albums, MediaItemId.ALBUM, R.string.title_albums),
                MediaItemInfo(R.string.title_artists, MediaItemId.ARTIST, R.string.title_artists),
                MediaItemInfo(
                    R.string.title_playlists,
                    MediaItemId.PLAYLIST,
                    R.string.title_playlists
                ),
                MediaItemInfo(R.string.title_genres, MediaItemId.GENRE, R.string.title_genres)
            )

        for (mediaItemInfo in mediaItemsInfo) {
            mediaItems.add(
                createMediaItemsRootCategories(
                    this,
                    mediaItemInfo.title,
                    mediaItemInfo.subtitle,
                    mediaItemInfo.id
                )
            )
        }
    }

    companion object {
        private const val MEDIA_ROOT_ID = "root"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root"
    }
}