package com.github.odaridavid.zikk.playback

import android.app.Activity
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import timber.log.Timber

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
class MediaBrowserConnectionCallback(
    val context: Context,
    private val activity: Activity,
    var mediaBrowser: MediaBrowserCompat?,
    private val mediaControllerCompatCallback: MediaControllerCompat.Callback
) : MediaBrowserCompat.ConnectionCallback() {

    /**
     *  Creates the media controller, links it to the media session with a session token,
     *  links your UI controls to the MediaController, and registers the controller to receive
     *  callbacks from the media session.
     */
    override fun onConnected() {
        super.onConnected()
        Timber.i("Connection with media browser successful")
        mediaBrowser?.run {
            val mediaControllerCompat = MediaControllerCompat(context, sessionToken)
            /* Save the controller */
            MediaControllerCompat.setMediaController(activity, mediaControllerCompat)

            mediaControllerCompat.registerCallback(mediaControllerCompatCallback)
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
}
