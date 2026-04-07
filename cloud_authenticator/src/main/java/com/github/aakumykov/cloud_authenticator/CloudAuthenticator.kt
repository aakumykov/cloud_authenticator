package com.github.aakumykov.cloud_authenticator

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

abstract class CloudAuthenticator() {

    open fun prepareResultLauncher(componentActivity: ComponentActivity): ActivityResultLauncher<Intent> {
        return componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            processAuthResult(result.resultCode, result.data)
        }
    }

    open fun prepareResultLauncher(fragment: Fragment): ActivityResultLauncher<Intent> {
        return fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            processAuthResult(result.resultCode, result.data)
        }
    }

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