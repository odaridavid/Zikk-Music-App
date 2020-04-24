package com.github.odaridavid.zikk.ui

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
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.base.BaseActivity
import com.github.odaridavid.zikk.data.ShowPlayerPreference
import com.github.odaridavid.zikk.mappers.PlayableTrack
import com.github.odaridavid.zikk.mappers.toTrack
import com.github.odaridavid.zikk.models.MediaId
import com.github.odaridavid.zikk.models.PlaybackStatus
import com.github.odaridavid.zikk.playback.session.ZikkMediaService
import com.github.odaridavid.zikk.utils.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Main screen on app launch
 *
 */
internal class DashboardActivity : BaseActivity(R.layout.activity_dashboard),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var showPlayerPreference: ShowPlayerPreference

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var mediaBrowser: MediaBrowserCompat? = null
    private var prevPlayingIndex = -1
    lateinit var playableTrack: PlayableTrack
    lateinit var playbackList: MutableList<PlayableTrack>

    //MediaPlayer UI
    private lateinit var nowPlayingCard: CardView
    private lateinit var trackArtistTextView: TextView
    private lateinit var trackTitleTextView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var playNextButton: ImageButton
    private lateinit var artImageView: ImageView

    private lateinit var mediaItemRecyclerView: RecyclerView
    private lateinit var dashboardProgressBar: ProgressBar
    private lateinit var tracksAdapter: TtacksAdapter
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(
            showPlayerPreference
        )
    }
    private var mediaControllerCompatCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_pause_black_48dp))
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_play_black_48dp))
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            super.onMetadataChanged(metadata)
            trackTitleTextView.text = metadata.title
            trackArtistTextView.text = metadata.artist
            artImageView.load(metadata.albumArtUri)
        }
    }

    private val mediaBrowserSubscriptionCallback =
        object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                playbackList = children.map { it.toTrack() }.toMutableList()
                dashboardProgressBar.hide()
                tracksAdapter.setList(playbackList)
            }

            override fun onError(parentId: String) {
                super.onError(parentId)
                Timber.d("Error on $parentId")
            }
        }

    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            Timber.i("Connection with media browser successful")
            dashboardViewModel.setIsConnected(true)
            mediaBrowser?.run {
                val mediaControllerCompat =
                    MediaControllerCompat(this@DashboardActivity, sessionToken)

                MediaControllerCompat.setMediaController(
                    this@DashboardActivity,
                    mediaControllerCompat
                )
                playPauseButton.setOnClickListener {
                    if (mediaControllerCompat.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
                        mediaTranspotControls?.pause()
                    else mediaTranspotControls?.play()
                }
                mediaControllerCompat.registerCallback(mediaControllerCompatCallback)
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Timber.i("Disconnected from media browser ")
            dashboardViewModel.setIsConnected(false)
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Timber.i("Connection with media browser failed")
            dashboardViewModel.setIsConnected(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        checkPermissionsAndInit(onPermissionNotGranted = {
            ActivityCompat.requestPermissions(
                this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
            )
        })
        initViews()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser?.connect()
        observeMediaBrowserConnection()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.checkPlayerStatus()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        mediaControllerCompat?.unregisterCallback(mediaControllerCompatCallback)
        mediaBrowser?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_STORAGE_PERMISSIONS) {
            checkPermissionsAndInit(onPermissionNotGranted = {
                showToast(getString(R.string.info_storage_permissions_not_granted))
                finish()
            })
        }
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String) {
        dashboardViewModel.checkPlayerStatus()
    }

    private fun initViews() {
        mediaItemRecyclerView = findViewById(R.id.tracks_recycler_view)
        dashboardProgressBar = findViewById(R.id.dashboard_progress_bar)
        trackArtistTextView = findViewById(R.id.track_artist_text_view)
        trackTitleTextView = findViewById(R.id.track_title_text_view)
        playPauseButton = findViewById(R.id.play_pause_button)
        playNextButton = findViewById(R.id.play_next_button)
        artImageView = findViewById(R.id.album_art_image_view)
        nowPlayingCard = findViewById(R.id.now_playing_card)
        tracksAdapter = TtacksAdapter { id, currentlyPlayingIndex, track ->
            val playbackStatus = PlaybackStatus(prevPlayingIndex, currentlyPlayingIndex)
            dashboardViewModel.setNowPlayingStatus(playbackStatus)
            //Update to Current Track
            prevPlayingIndex = currentlyPlayingIndex
            playableTrack = track

            dashboardViewModel.setPlayerActive()

            mediaTranspotControls?.playFromMediaId(id, null)
        }
        mediaItemRecyclerView.adapter = tracksAdapter

        observeNowPlayingIcon()
        observePlayerActivity()
    }

    private fun observeNowPlayingIcon() {
        dashboardViewModel.playbackStatus.observe(this, Observer { playbackStatus ->
            if (playbackStatus.prevTrackIndex != -1) {
                tracksAdapter.updateIsPlaying(playbackStatus.prevTrackIndex, false)
                tracksAdapter.updateIsPlaying(playbackStatus.currentTrackIndex, true)
            } else {
                tracksAdapter.updateIsPlaying(playbackStatus.currentTrackIndex, true)
            }
        })
    }

    private fun initMediaBrowser() {
        val cn = ComponentName(this, ZikkMediaService::class.java)
        mediaBrowser = MediaBrowserCompat(this, cn, mediaBrowserConnectionCallback, null)
    }

    private fun observeMediaBrowserConnection() {
        dashboardViewModel.isMediaBrowserConnected.observe(this, Observer { isConnected ->
            if (!isConnected) mediaBrowser?.run { unsubscribe(root) }
            else mediaBrowser?.subscribe(MediaId.TRACK.toString(), mediaBrowserSubscriptionCallback)
        })
    }

    private fun observePlayerActivity() {
        dashboardViewModel.playerActive.observe(this, Observer { isActive ->
            if (isActive)
                nowPlayingCard.show()
            else
                nowPlayingCard.hide()
        })
    }

    private fun checkPermissionsAndInit(onPermissionNotGranted: () -> Unit) {
        if (!PermissionUtils.allPermissionsGranted(this, PermissionUtils.STORAGE_PERMISSIONS))
            onPermissionNotGranted()
        else initMediaBrowser()
    }

    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
    }
}
