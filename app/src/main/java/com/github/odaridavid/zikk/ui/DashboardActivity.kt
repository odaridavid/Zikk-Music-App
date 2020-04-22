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
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.base.BaseActivity
import com.github.odaridavid.zikk.playback.MediaId
import com.github.odaridavid.zikk.playback.controller.MediaControllerCompatCallback
import com.github.odaridavid.zikk.playback.session.ZikkMediaService
import com.github.odaridavid.zikk.utils.hide
import com.github.odaridavid.zikk.utils.injector
import com.github.odaridavid.zikk.utils.mediaControllerCompat
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import timber.log.Timber

/**
 * Main screen on app launch
 */
internal class DashboardActivity : BaseActivity(R.layout.activity_dashboard) {

    //TODO Control player through media controller and display current state
    //TODO DI and Code Cleanup
    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaControllerCompatCallback = MediaControllerCompatCallback()

    private lateinit var mediaItemRecyclerView: RecyclerView
    private lateinit var dashboardProgressBar: ProgressBar
    private lateinit var mediaItemAdapter: MediaItemAdapter
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        initViews()
        initMediaBrowser()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser?.connect()
        observeMediaBrowserConnection()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        mediaControllerCompat?.unregisterCallback(mediaControllerCompatCallback)
        mediaBrowser?.disconnect()
    }

    private fun initViews() {
        mediaItemRecyclerView = findViewById(R.id.tracks_recycler_view)
        dashboardProgressBar = findViewById(R.id.dashboard_progress_bar)
        mediaItemAdapter = MediaItemAdapter { id ->
            //TODO Browse content with media browser service
        }
        mediaItemRecyclerView.adapter = ScaleInAnimationAdapter(mediaItemAdapter)
    }

    private fun initMediaBrowser() {
        //Identifier for MBS
        val cn = ComponentName(this, ZikkMediaService::class.java)

        mediaBrowser =
            MediaBrowserCompat(this, cn, object : MediaBrowserCompat.ConnectionCallback() {

                override fun onConnected() {
                    super.onConnected()
                    Timber.i("Connection with media browser successful")
                    dashboardViewModel.setIsConnected(true)
                    mediaBrowser?.run {
                        val mediaControllerCompat =
                            MediaControllerCompat(this@DashboardActivity, sessionToken)
                        /* Save the controller */
                        MediaControllerCompat.setMediaController(
                            this@DashboardActivity,
                            mediaControllerCompat
                        )

                        mediaControllerCompat.registerCallback(mediaControllerCompatCallback)

                    }
                }

                override fun onConnectionSuspended() {
                    super.onConnectionSuspended()
                    dashboardViewModel.setIsConnected(false)
                    Timber.i("Disconnected from media browser ")
                }

                override fun onConnectionFailed() {
                    super.onConnectionFailed()
                    dashboardViewModel.setIsConnected(false)
                    Timber.i("Connection with media browser failed")
                }
            }, null)
    }

    private fun observeMediaBrowserConnection() {
        //After the client connects, it can traverse the content hierarchy by making repeated calls
        //to MediaBrowserCompat.subscribe() to build a local representation of the UI
        dashboardViewModel.isMediaBrowserConnected.observe(this, Observer { isConnected ->
            if (!isConnected) mediaBrowser?.run { unsubscribe(root) }
            else mediaBrowser?.subscribe(
                MediaId.TRACK.toString(),
                object : MediaBrowserCompat.SubscriptionCallback() {

                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {
                        super.onChildrenLoaded(parentId, children)
                        Timber.d("$parentId Loaded with ${children.count()} items")
                        dashboardProgressBar.hide()
                        mediaItemAdapter.submitList(children)
                    }

                    override fun onError(parentId: String) {
                        super.onError(parentId)
                        Timber.d("Error on $parentId")
                    }
                })
        })
    }
}
