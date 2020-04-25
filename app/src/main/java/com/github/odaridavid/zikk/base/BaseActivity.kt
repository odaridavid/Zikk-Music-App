package com.github.odaridavid.zikk.base

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.odaridavid.zikk.notification.NotificationsChannelManager
import com.github.odaridavid.zikk.notification.NotificationsChannelManager.Companion.PLAYBACK_CHANNEL_ID
import com.github.odaridavid.zikk.utils.injector
import com.github.odaridavid.zikk.utils.versionFrom
import javax.inject.Inject

internal abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var notificationsChannelManager: NotificationsChannelManager


    override fun onCreate(savedInstanceState: Bundle?) {
        matchStatusBarWithBackground()
        injector.inject(this)
        super.onCreate(savedInstanceState)
        initNotificationChannel()
    }

    private fun matchStatusBarWithBackground() {
        if (versionFrom(Build.VERSION_CODES.M)) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(android.R.color.background_light)
        }
    }

    private fun initNotificationChannel() {
        with(notificationsChannelManager) {
            if (versionFrom(Build.VERSION_CODES.O) && !hasChannel(PLAYBACK_CHANNEL_ID))
                createNotificationChannel(PLAYBACK_CHANNEL_ID)
        }
    }
}
