package com.github.odaridavid.zikk.utils

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
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

fun View.show() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Context.showDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    positiveBlock: () -> (Unit),
    negativeBlock: () -> (Unit)
) {
    AlertDialog.Builder(this)
        .setTitle(getString(title))
        .setMessage(getString(message))
        .setPositiveButton(android.R.string.yes) { dialog, _ ->
            positiveBlock()
            dialog.dismiss()
        }
        .setNegativeButton(android.R.string.no) { dialog, _ ->
            negativeBlock()
            dialog.dismiss()
        }
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show().apply {
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

}