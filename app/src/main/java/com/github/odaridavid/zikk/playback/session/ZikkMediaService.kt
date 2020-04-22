package com.github.odaridavid.zikk.playback.session

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
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.playback.BecomingNoisyReceiver
import com.github.odaridavid.zikk.playback.MediaCategoryInfo
import com.github.odaridavid.zikk.playback.MediaId
import com.github.odaridavid.zikk.playback.notification.PlaybackNotificationBuilder
import com.github.odaridavid.zikk.repositories.*
import com.github.odaridavid.zikk.utils.convertMillisecondsToDuration
import com.github.odaridavid.zikk.utils.createMediaItem
import com.github.odaridavid.zikk.utils.getDrawableUri
import com.github.odaridavid.zikk.utils.injector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Media Browser Service which controls the media session and co-ordinates with the media
 * browser attached through the media player.
 * @see <a href="Media App Architecture">https://developer.android.com/guide/topics/media-apps/media-apps-overview</a>
 */
internal class ZikkMediaService : MediaBrowserServiceCompat() {
    //TODO Optimize for android auto and wearables
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
                audioManager,
                becomingNoisyReceiver
            )
        )
        //TODO Set track player data source to play
        // trackPlayer.setDataSource(applicationContext, contentUri)

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
            buildMediaCategories(mediaItems)
            result.sendResult(mediaItems)
        } else {

            result.detach()
            //TODO Create coroutine scope that is cancelable and launch work in scope
            GlobalScope.launch(Dispatchers.IO) {
                val mediaItemId = MediaId.values().find { id -> id.toString() == parentId }
                getChildren(mediaItems, mediaItemId)
                result.sendResult(mediaItems)
            }
        }
    }

    // Examine the passed parentId to see which submenu we're at,and put the children of that menu in the mediaItems list
    private fun getChildren(
        mediaItems: MutableList<MediaBrowserCompat.MediaItem>,
        mediaId: MediaId?
    ) {
        if (mediaId != null)
            when (mediaId) {
                MediaId.ALBUM -> {
                    val albums = albumProvider.loadAllAlbums()
                    albums.forEach { album ->
                        mediaItems.add(
                            createMediaItem(
                                album.title,
                                resources.getQuantityString(
                                    R.plurals.number_of_songs,
                                    album.noOfSongs,
                                    album.noOfSongs
                                ),
                                "${MediaId.ALBUM}${album.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.ARTIST -> {
                    val artists = artistProvider.loadAllArtists()
                    artists.forEach { artist ->
                        mediaItems.add(
                            createMediaItem(
                                artist.name,
                                resources.getQuantityString(
                                    R.plurals.number_of_albums,
                                    artist.noOfAlbums,
                                    artist.noOfAlbums
                                ),
                                "${MediaId.ARTIST}${artist.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.GENRE -> {
                    val genres = genreProvider.loadAllGenres()
                    genres.forEach { genre ->
                        mediaItems.add(
                            createMediaItem(
                                genre.name,
                                "",
                                "${MediaId.GENRE}${genre.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )

                    }
                }
                MediaId.PLAYLIST -> {
                    val playlists = playlistProvider.loadAllPlaylists()
                    playlists.forEach { playlist ->
                        mediaItems.add(
                            createMediaItem(
                                playlist.name,
                                playlist.modified,
                                "${MediaId.PLAYLIST}${playlist.id}",
                                null,
                                "",
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
                MediaId.TRACK -> {
                    val tracks = trackProvider.loadAllTracks()
                    tracks.forEach { track ->
                        mediaItems.add(
                            createMediaItem(
                                track.title,
                                track.artist,
                                "${MediaId.TRACK}${track.id}",
                                Uri.parse(track.albumArt),
                                convertMillisecondsToDuration(track.duration.toLong()),
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
        TrackPlayer.release()
        mediaSessionCompat.release()
    }

    private fun buildMediaCategories(mediaItems: MutableList<MediaBrowserCompat.MediaItem>) {
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

    companion object {
        private const val MEDIA_ROOT_ID = "root"
        private const val EMPTY_MEDIA_ROOT_ID = "empty_root"
    }
}