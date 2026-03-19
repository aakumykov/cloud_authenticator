package com.github.aakumykov.local_authenticator

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator

class LocalAuthenticator(private val cloudAuthenticatorCallbacks: Callbacks) : CloudAuthenticator() {

    override fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        cloudAuthenticatorCallbacks.onCloudAuthSuccess(DUMMY_AUTH_TOKEN)
    }

    override fun processAuthResult(resultCode: Int, data: Intent?) {
        cloudAuthenticatorCallbacks.onCloudAuthSuccess(DUMMY_AUTH_TOKEN)
    }

    override fun deAuth() {
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }

    companion object {
        const val DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN"
    }
}