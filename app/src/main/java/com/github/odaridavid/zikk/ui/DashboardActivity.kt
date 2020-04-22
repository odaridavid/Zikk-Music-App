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
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.base.BaseActivity
import com.github.odaridavid.zikk.playback.controller.MediaBrowserSubscriptionCallback
import com.github.odaridavid.zikk.playback.controller.MediaControllerCompatCallback
import com.github.odaridavid.zikk.playback.session.ZikkMediaService
import com.github.odaridavid.zikk.utils.injector
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
    private var mediaBrowserSubscriptionCallback = MediaBrowserSubscriptionCallback()

    private lateinit var mediaItemRecyclerView: RecyclerView
    private lateinit var dashboardProgressBar: ProgressBar
    private lateinit var mediaItemAdapter: MediaItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        mediaItemRecyclerView = findViewById(R.id.tracks_recycler_view)
        dashboardProgressBar = findViewById(R.id.dashboard_progress_bar)
        mediaItemAdapter = MediaItemAdapter { id ->
            //TODO Browse content with media browser service
        }
        mediaItemRecyclerView.adapter = ScaleInAnimationAdapter(mediaItemAdapter)
    }

    override fun onStart() {
        super.onStart()
        initMediaBrowser()
    }

    private fun initMediaBrowser() {
        //Identifier for MBS
        val cn = ComponentName(this, ZikkMediaService::class.java)

        mediaBrowser =
            MediaBrowserCompat(this, cn, object : MediaBrowserCompat.ConnectionCallback() {

                override fun onConnected() {
                    super.onConnected()
                    Timber.i("Connection with media browser successful")
                    mediaBrowser?.run {
                        val mediaControllerCompat =
                            MediaControllerCompat(this@DashboardActivity, sessionToken)
                        /* Save the controller */
                        MediaControllerCompat.setMediaController(
                            this@DashboardActivity,
                            mediaControllerCompat
                        )

                        mediaControllerCompat.registerCallback(mediaControllerCompatCallback)

                        //After the client connects, it can traverse the content hierarchy by making repeated calls to MediaBrowserCompat.subscribe() to build a local representation of the UI
                        subscribe(root, mediaBrowserSubscriptionCallback)
                    }
                }

                override fun onConnectionSuspended() {
                    super.onConnectionSuspended()
                    Timber.i("Disconnected from media browser ")
                    mediaBrowser?.run { unsubscribe(root) }
                }

                override fun onConnectionFailed() {
                    super.onConnectionFailed()
                    Timber.i("Connection with media browser failed")
                }
            }, null)
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }


}
