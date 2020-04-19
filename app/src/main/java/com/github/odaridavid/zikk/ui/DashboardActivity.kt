package com.github.odaridavid.zikk.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.odaridavid.zikk.R
import com.github.odaridavid.zikk.utils.PermissionUtils
import com.github.odaridavid.zikk.utils.showToast
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter

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
class DashboardActivity : AppCompatActivity(R.layout.activity_dashboard) {

    lateinit var recentsRecyclerView: RecyclerView
    lateinit var songsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

//        recentsRecyclerView.adapter = ScaleInAnimationAdapter(RecentsAdapter())
//        songsRecyclerView.adapter = ScaleInAnimationAdapter(SongsAdapter())

        checkPermissions(onPermissionNotGranted = {
            ActivityCompat.requestPermissions(
                this, PermissionUtils.STORAGE_PERMISSIONS, RQ_STORAGE_PERMISSIONS
            )
        })
    }

    private inline fun checkPermissions(onPermissionNotGranted: () -> Unit) {
        if (!PermissionUtils.allPermissionsGranted(this, PermissionUtils.STORAGE_PERMISSIONS))
            onPermissionNotGranted()
    }

    private fun initViews() {
        recentsRecyclerView = findViewById(R.id.recents_recycler_view)
        songsRecyclerView = findViewById(R.id.songs_recycler_view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_STORAGE_PERMISSIONS) {
            checkPermissions(onPermissionNotGranted = {
                showToast(getString(R.string.info_storage_permissions_not_granted))
                finish()
            })
        }
    }

    companion object {
        private const val RQ_STORAGE_PERMISSIONS = 1000
    }
}
