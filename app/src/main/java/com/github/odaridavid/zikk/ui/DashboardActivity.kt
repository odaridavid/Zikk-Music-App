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
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.playback.MediaBrowserLifecycleObserver
import com.github.odaridavid.zikk.playback.ZikkMediaService
import com.github.odaridavid.zikk.tracks.TrackRepository
import com.github.odaridavid.zikk.tracks.TracksAdapter
import com.github.odaridavid.zikk.utils.PermissionUtils
import com.github.odaridavid.zikk.utils.injector
import com.github.odaridavid.zikk.utils.showToast
import com.github.odaridavid.zikk.utils.versionFrom
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main screen on app launch
 */
internal class DashboardActivity : AppCompatActivity(R.layout.activity_dashboard) {

    //TODO DI and UI Cleanup
    //TODO Check on Media Button Receiver
    private lateinit var mediaBrowser: MediaBrowserCompat

    @Inject
    lateinit var tracksRepository: TrackRepository

    private lateinit var tracksRecyclerView: RecyclerView

    private lateinit var tracksAdapter: TracksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        if (versionFrom(Build.VERSION_CODES.M)) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(android.R.color.background_light)
        }
        initViews()
        checkPermissionsAndInit(onPermissionNotGranted = {
            ActivityCompat.requestPermissions(
                this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
            )
        })
    }

    private fun initViews() {
        tracksRecyclerView = findViewById(R.id.tracks_recycler_view)
        tracksAdapter = TracksAdapter()
        tracksRecyclerView.adapter = ScaleInAnimationAdapter(tracksAdapter)
    }

    private inline fun checkPermissionsAndInit(onPermissionNotGranted: () -> Unit) {
        if (!PermissionUtils.allPermissionsGranted(this, PermissionUtils.STORAGE_PERMISSIONS))
            onPermissionNotGranted()
        else {
            //TODO Cleanup move logic to viewmodel and show loading state while fetching tracks
            GlobalScope.launch(Dispatchers.IO) {
                val tracks = tracksRepository.getAllTracks()
                withContext(Dispatchers.Main) {
                    tracksAdapter.submitList(tracks)
                }
            }

            mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, ZikkMediaService::class.java),
                object : MediaBrowserCompat.ConnectionCallback() {

                    /**
                     *  Creates the media controller, links it to the media session with a session token,
                     *  links your UI controls to the MediaController, and registers the controller to receive
                     *  callbacks from the media session.
                     */
                    override fun onConnected() {
                        super.onConnected()
                        mediaBrowser.sessionToken.also { token ->
                            val mediaController =
                                MediaControllerCompat(this@DashboardActivity, token)

                            /* Save the controller */
                            MediaControllerCompat.setMediaController(
                                this@DashboardActivity,
                                mediaController
                            )
                        }

                        buildTransportControls()
                    }
                },
                null
            )
            lifecycle.addObserver(MediaBrowserLifecycleObserver(mediaBrowser))
        }
    }


    override fun onResume() {
        super.onResume()
        //Audio stream to change when handling hardware volume changes
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
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

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this)
        // Grab the view for the play/pause button
//        findViewById<ImageView>(R.id.dashboard_playback_button).apply {
//            setOnClickListener {
//                // Since this is a play/pause button, you'll need to test the current state
//                // and choose the action accordingly
//                val pbState = mediaController.playbackState.state
//                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    mediaController.transportControls.pause()
//                } else {
//                    mediaController.transportControls.play()
//                }
//            }
//        }

        // TODO Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }


    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
        private val controllerCallback = object : MediaControllerCompat.Callback() {

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                //TODO Update Metadata
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                //TODO Update UI with Playbackstate
            }
        }
    }
}
