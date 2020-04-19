package com.github.odaridavid.zikk.playback

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*

/**
 * Copyright 2020 David Odari
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
internal object PlaybackStateBuilder {
    val instance: PlaybackStateCompat = Builder()
        .setActions(
            ACTION_PLAY or ACTION_PLAY_PAUSE or ACTION_PAUSE or ACTION_STOP or ACTION_SKIP_TO_PREVIOUS or ACTION_SKIP_TO_NEXT
        )
        .build()
}