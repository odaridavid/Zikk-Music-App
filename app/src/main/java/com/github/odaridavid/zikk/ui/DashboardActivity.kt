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

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import coil.api.load
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.base.BaseActivity
import com.github.odaridavid.zikk.data.LastPlayedTrackPreference
import com.github.odaridavid.zikk.databinding.ActivityDashboardBinding
import com.github.odaridavid.zikk.mappers.toTrack
import com.github.odaridavid.zikk.models.MediaId
import com.github.odaridavid.zikk.models.PlayableTrack
import com.github.odaridavid.zikk.models.PlaybackStatus
import com.github.odaridavid.zikk.playback.session.ZikkMediaService
import com.github.odaridavid.zikk.utils.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Main screen on app launch
 *
 */
internal class DashboardActivity : BaseActivity() {

    @Inject
    lateinit var lastPlayedTrackPreference: LastPlayedTrackPreference

    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var playableTrack: PlayableTrack
    private lateinit var playbackList: MutableList<PlayableTrack>
    lateinit var dashboardBinding: ActivityDashboardBinding
    private lateinit var tracksAdapter: TracksAdapter
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(
            lastPlayedTrackPreference
        )
    }
    private var mediaControllerCompatCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
            super.onPlaybackStateChanged(playbackState)
            Timber.d("Playback changed")
            handlePlaybackState(playbackState.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            super.onMetadataChanged(metadata)
            Timber.d("Metadata changed")
            bindMetadataToViews(metadata)
        }
    }

    private val mediaBrowserSubscriptionCallback =
        object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                Timber.i("$parentId children loaded.")
                playbackList = children.map { it.toTrack() }.toMutableList()
                dashboardBinding.dashboardProgressBar.hide()
                tracksAdapter.setList(playbackList)
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
                mediaControllerCompat.playbackState?.let { state ->
                    handlePlaybackState(state.state)
                }
                mediaControllerCompat.metadata?.let { metadata ->
                    bindMetadataToViews(metadata)
                }
                initPlayPause(mediaControllerCompat)
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
        dashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = dashboardBinding.root
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndInit()
        initAdapter()
    }

    override fun onResume() {
        super.onResume()
        if (mediaBrowser?.isConnected == true)
            mediaControllerCompat.playbackState?.let { state ->
                handlePlaybackState(state.state)
            }
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun initPlayPause(mediaControllerCompat: MediaControllerCompat) {
        dashboardBinding.playPauseButton.setOnClickListener {
            mediaControllerCompat.playbackState?.let { state ->
                if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                    mediaTranspotControls?.pause()
                    handlePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                } else {
                    mediaTranspotControls?.play()
                    handlePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser?.disconnect()
        mediaControllerCompat.unregisterCallback(mediaControllerCompatCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RQ_STORAGE_PERMISSIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Timber.i("Storage Permissions Granted")
                initMediaBrowser()
            } else {
                Timber.i("Storage Permissions Not Granted")
                onPermissionNotGranted()
            }
        }
    }

    private fun initAdapter() {
        tracksAdapter = TracksAdapter { id, currentlyPlayingIndex, track ->
            val playbackStatus = PlaybackStatus(prevPlayingIndex, currentlyPlayingIndex)
            dashboardViewModel.setNowPlayingStatus(playbackStatus)
            //Update to Current Track
            prevPlayingIndex = currentlyPlayingIndex
            playableTrack = track
            dashboardViewModel.setCurrentlyPlayingTrackId(convertMediaIdToTrackId(id!!))
            mediaTranspotControls?.playFromMediaId(id, null)
        }
        dashboardBinding.tracksRecyclerView.adapter = tracksAdapter
        observeNowPlayingIcon()
    }

    private fun bindMetadataToViews(metadata: MediaMetadataCompat) {
        dashboardBinding.trackTitleTextView.text = metadata.title
        dashboardBinding.trackArtistTextView.text = metadata.artist
        dashboardBinding.albumArtImageView.load(metadata.albumArtUri)
        dashboardBinding.nowPlayingCard.show()
    }

    private fun handlePlaybackState(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                dashboardBinding.playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_pause_black_48dp))
                tracksAdapter.updateIsPlaying(prevPlayingIndex, true)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                dashboardBinding.playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_play_black_48dp))
                tracksAdapter.updateIsPlaying(prevPlayingIndex, false)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                dashboardBinding.nowPlayingCard.hide()
                tracksAdapter.updateIsPlaying(prevPlayingIndex, false)
            }
            PlaybackStateCompat.STATE_ERROR -> {
                Timber.d("State error")
            }
        }
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
        observeMediaBrowserConnection()
        mediaBrowser!!.connect()
    }

    private fun observeMediaBrowserConnection() {
        dashboardViewModel.isMediaBrowserConnected.observe(this, Observer { isConnected ->
            if (!isConnected) mediaBrowser?.run { unsubscribe(root) }
            else mediaBrowser?.subscribe(MediaId.TRACK.toString(), mediaBrowserSubscriptionCallback)
        })
    }


    private fun checkPermissionsAndInit() {
        if (!PermissionUtils.checkAllPermissionsGranted(
                this,
                PermissionUtils.STORAGE_PERMISSIONS
            )
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showDialog(
                    title = R.string.title_need_storage_permission,
                    message = R.string.info_storage_permission_reason,
                    positiveBlock = ::requestStoragePermissions,
                    negativeBlock = ::onPermissionNotGranted
                )
            } else {
                requestStoragePermissions()
            }
        } else initMediaBrowser()
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
        )
    }

    private fun onPermissionNotGranted() {
        dashboardBinding.dashboardProgressBar.hide()
        showToast(getString(R.string.info_storage_permissions_not_granted))
        finish()
    }

    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
        private var prevPlayingIndex = -1
    }
}
