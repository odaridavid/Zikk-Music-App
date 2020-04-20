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
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.playback.TrackPlayer
import com.github.odaridavid.zikk.tracks.TrackRepository
import com.github.odaridavid.zikk.tracks.TracksAdapter
import com.github.odaridavid.zikk.utils.*
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import javax.inject.Inject

/**
 * Main screen on app launch
 */
internal class DashboardActivity : AppCompatActivity(R.layout.activity_dashboard) {

    //TODO Bind Media player to service with media controls
    //TODO DI and Code Cleanup
    private lateinit var mediaBrowser: MediaBrowserCompat

    @Inject
    lateinit var trackPlayer: TrackPlayer

    @Inject
    lateinit var tracksRepository: TrackRepository

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var dashboardProgressBar: ProgressBar
    private lateinit var tracksAdapter: TracksAdapter
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(
            tracksRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        matchStatusBarWithBackground()
        super.onCreate(savedInstanceState)
        init()
        checkPermissionsAndInit(onPermissionNotGranted = {
            ActivityCompat.requestPermissions(
                this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
            )
        })
    }

    private fun matchStatusBarWithBackground() {
        if (versionFrom(Build.VERSION_CODES.M)) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(android.R.color.background_light)
        }
    }

    private fun init() {
        tracksRecyclerView = findViewById(R.id.tracks_recycler_view)
        dashboardProgressBar = findViewById(R.id.dashboard_progress_bar)
        tracksAdapter = TracksAdapter { id ->
            val contentUri: Uri =
                ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            trackPlayer.reset()
            trackPlayer.setDataSource(applicationContext, contentUri)
            trackPlayer.prepare()
        }
        tracksRecyclerView.adapter = ScaleInAnimationAdapter(tracksAdapter)
        observeTracks()
    }

    private inline fun checkPermissionsAndInit(onPermissionNotGranted: () -> Unit) {
        if (!PermissionUtils.allPermissionsGranted(this, PermissionUtils.STORAGE_PERMISSIONS))
            onPermissionNotGranted()
        else dashboardViewModel.loadTracks()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
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

    override fun onDestroy() {
        super.onDestroy()
        trackPlayer.release()
    }

    private fun observeTracks() {
        dashboardViewModel.tracks.observe(this, Observer { tracks ->
            dashboardProgressBar.hide()
            tracksAdapter.submitList(tracks)
        })
    }

    fun observeMetadata() {
        dashboardViewModel.nowPlaying.observe(this, Observer {
            //TODO Update UI
        })
    }

    fun observePlaybackState() {
        dashboardViewModel.playbackState.observe(this, Observer {
            //TODO Update playback state
        })
    }

    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
    }
}
