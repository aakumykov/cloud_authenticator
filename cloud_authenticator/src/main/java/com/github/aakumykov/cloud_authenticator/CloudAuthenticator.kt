package com.github.aakumykov.cloud_authenticator

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

abstract class CloudAuthenticator() {

    abstract fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>)
    abstract fun processAuthResult(resultCode: Int, data: Intent?)
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