package com.github.aakumykov.local_authenticator

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.github.aakumykov.storage_access_helper.StorageAccessHelper

class LocalAuthenticator(
    private val componentActivity: ComponentActivity,
    private val cloudAuthenticatorCallbacks: Callbacks
) : CloudAuthenticator() {

    private val storageAccessHelper by lazy {
        StorageAccessHelper.create(componentActivity).apply {
            prepareForFullAccess()
        }
    }

    override fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        storageAccessHelper.requestFullAccess {
            if (it) cloudAuthenticatorCallbacks.onCloudAuthSuccess(DUMMY_AUTH_TOKEN)
            else cloudAuthenticatorCallbacks.onCloudAuthFailed(Exception("Storage access denied"))
        }
    }

    override fun processAuthResult(resultCode: Int, data: Intent?) {

    }

    override fun deAuth() {
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }

    companion object {
        const val DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN"
    }
}