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
 *
 **/
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v4.media.session.MediaControllerCompat
import javax.inject.Inject

/**
 * @see <a href="https://developer.android.com/guide/topics/media-apps/volume-and-earphones#becoming-noisy">Don't be noisy</a>
 */
class BecomingNoisyReceiver @Inject constructor(private val mediaControllerCompat: MediaControllerCompat) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            mediaControllerCompat.transportControls.pause()
        }
    }
}