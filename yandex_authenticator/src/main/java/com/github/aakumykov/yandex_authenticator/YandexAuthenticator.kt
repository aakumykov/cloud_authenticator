package com.github.aakumykov.yandex_authenticator

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdkContract
import com.yandex.authsdk.internal.strategy.LoginType

typealias YandexLoginType = LoginType

class YandexAuthenticator(
    private val loginType: LoginType,
    componentActivity: ComponentActivity,
    cloudAuthenticatorCallbacks: Callbacks
) : CloudAuthenticator() {
    private val activityResultLauncher: ActivityResultLauncher<YandexAuthLoginOptions>

    init {
        val yandexAuthOptions = YandexAuthOptions(componentActivity, true)
        val yandexAuthSdkContract = YandexAuthSdkContract(yandexAuthOptions)

        activityResultLauncher = componentActivity.registerForActivityResult(yandexAuthSdkContract) { result: YandexAuthResult ->
            when(result) {
                is YandexAuthResult.Success -> cloudAuthenticatorCallbacks.onCloudAuthSuccess(result.token.value)
                is YandexAuthResult.Failure -> cloudAuthenticatorCallbacks.onCloudAuthFailed(result.exception)
                else -> cloudAuthenticatorCallbacks.onCloudAuthCancelled()
            }
        }
    }

    override fun deAuth() {
        Log.i(TAG, "Unauthorizing not implemented")
    }

    override fun startAuth() {
        activityResultLauncher.launch(
            YandexAuthLoginOptions(convertLoginType(loginType))
        )
    }


    private fun convertLoginType(loginType: LoginType): YandexLoginType {
        return when(loginType) {
            LoginType.WEBVIEW -> YandexLoginType.WEBVIEW
            LoginType.NATIVE -> YandexLoginType.NATIVE
        }
    }

    companion object {
        val TAG: String = YandexAuthenticator::class.java.simpleName
    }
}