package com.github.aakumykov.local_authenticator

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.github.aakumykov.storage_access_helper.StorageAccessHelper

class LocalAuthenticator(
    private val cloudAuthenticatorCallbacks: Callbacks
) : CloudAuthenticator() {

    private var _storageAccessHelper: StorageAccessHelper? = null
    private val storageAccessHelper: StorageAccessHelper get() = _storageAccessHelper!!

    override fun prepareResultLauncher(componentActivity: ComponentActivity): ActivityResultLauncher<Intent> {
        _storageAccessHelper = StorageAccessHelper.create(componentActivity).apply {
            prepareForFullAccess()
        }
        return super.prepareResultLauncher(componentActivity)
    }

    override fun prepareResultLauncher(fragment: Fragment): ActivityResultLauncher<Intent> {
        _storageAccessHelper = StorageAccessHelper.create(fragment).apply {
            prepareForFullAccess()
        }
        return super.prepareResultLauncher(fragment)
    }

    override fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        storageAccessHelper.requestFullAccess { isGranted ->
            if (isGranted) cloudAuthenticatorCallbacks.onCloudAuthSuccess(DUMMY_AUTH_TOKEN)
            else cloudAuthenticatorCallbacks.onCloudAuthFailed(Exception("Access to storage denied."))
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