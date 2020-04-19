package com.github.odaridavid.zikk.playback

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import javax.inject.Inject

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
internal class MediaSessionCallback(
    private val zikkMediaService: ZikkMediaService
) : MediaSessionCompat.Callback() {

    @Inject
    lateinit var playbackNotificationBuilder: PlaybackNotificationBuilder

    override fun onPlay() {
        super.onPlay()
        //This ensures that the service starts and continues to run, even when all UI MediaBrowser activities that are bound to it unbind.
        with(zikkMediaService) {
            startService(Intent(applicationContext, zikkMediaService::class.java))
            playbackNotificationBuilder.buildNotification()
        }

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        // A started service must be explicitly stopped, whether or not it's bound. This ensures that your player continues to perform even if the controlling UI activity unbinds
        zikkMediaService.stopSelf()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
    }
}