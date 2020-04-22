package com.github.odaridavid.zikk.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.PermissionUtils
import com.github.odaridavid.zikk.utils.showToast
import com.github.odaridavid.zikk.utils.versionFrom

internal abstract class BaseActivity(@LayoutRes layout:Int) : AppCompatActivity(layout) {

    override fun onCreate(savedInstanceState: Bundle?) {
        matchStatusBarWithBackground()
        super.onCreate(savedInstanceState)
        checkPermissionsAndInit(onPermissionNotGranted = {
            ActivityCompat.requestPermissions(
                this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
            )
        })
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

    private fun matchStatusBarWithBackground() {
        if (versionFrom(Build.VERSION_CODES.M)) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(android.R.color.background_light)
        }
    }

    private fun checkPermissionsAndInit(onPermissionNotGranted: () -> Unit) {
        if (!PermissionUtils.allPermissionsGranted(this, PermissionUtils.STORAGE_PERMISSIONS))
            onPermissionNotGranted()
    }

    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
    }
}
