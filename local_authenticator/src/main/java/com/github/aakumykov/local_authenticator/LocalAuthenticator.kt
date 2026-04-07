package com.github.aakumykov.local_authenticator

import android.R.attr.fragment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator

class LocalAuthenticator(private val cloudAuthenticatorCallbacks: Callbacks) : CloudAuthenticator() {

    private var activityResultLauncher: ActivityResultLauncher<String>? = null

    private val activityResultCallback = { isGranted: Boolean ->
        if (isGranted) {
            cloudAuthenticatorCallbacks.onCloudAuthSuccess(DUMMY_AUTH_TOKEN)
        } else {
            // TODO: спец.исключение
            cloudAuthenticatorCallbacks.onCloudAuthFailed(Exception("Permission denied."))
        }
    }

    override fun prepare(componentActivity: ComponentActivity) {
        activityResultLauncher = componentActivity.registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            callback = activityResultCallback
        )
    }

    override fun prepare(fragment: Fragment) {
        activityResultLauncher = fragment.registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            callback = activityResultCallback
        )
    }

    override fun startAuth() {
        activityResultLauncher?.launch(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    }

    override fun deAuth() {
        // TODO: вызывать настройки?
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }

    companion object {
        const val DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN"
    }
}