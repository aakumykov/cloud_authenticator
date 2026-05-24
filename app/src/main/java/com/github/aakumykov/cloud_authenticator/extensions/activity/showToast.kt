package com.github.aakumykov.cloud_authenticator.extensions.activity

import android.app.Activity
import android.widget.Toast
import androidx.annotation.StringRes

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Activity.showToast(@StringRes strRes: Int) {
    showToast(getString(strRes))
}