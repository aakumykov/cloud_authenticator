package com.github.aakumykov.cloud_authenticator

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

abstract class CloudAuthenticator() {

    abstract fun prepare(componentActivity: ComponentActivity)
    abstract fun prepare(fragment: Fragment)

    abstract fun startAuth()

    // TODO: переименовать в clearAuth или forgetAuth
    abstract fun deAuth()

    interface Callbacks {
        fun onCloudAuthSuccess(authToken: String)
        fun onCloudAuthFailed(throwable: Throwable)
        fun onCloudAuthCancelled()

        fun onDeAuthSuccess()
        fun onDeAuthCancelled(message: String? = null)
        fun onDeAuthError(throwable: Throwable)
    }

    enum class LoginType {
        NATIVE,
        WEBVIEW
    }
}