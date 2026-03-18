package com.github.aakumykov.yandex_authenticator

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdkContract
import com.yandex.authsdk.internal.strategy.LoginType

typealias YandexLoginType = com.yandex.authsdk.internal.strategy.LoginType

class YandexAuthenticator(
    loginType: LoginType,
    private val context: Context,
    private val cloudAuthenticatorCallbacks: Callbacks
)
    : CloudAuthenticator()
{
    val yandexAuthOptions: YandexAuthLoginOptions
    val yandexAuthSdkContract: YandexAuthSdkContract

    init {
        yandexAuthOptions = YandexAuthLoginOptions(convertLoginType(loginType))
        yandexAuthSdkContract = YandexAuthSdkContract(YandexAuthOptions(context, true))
    }

    override fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        activityResultLauncher.launch(
            yandexAuthSdkContract.createIntent(context,yandexAuthOptions)
        )
    }

    override fun deAuth() {
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }

    override fun processAuthResult(resultCode: Int, data: Intent?) {
        val authResult = yandexAuthSdkContract.parseResult(resultCode, data)
        when(authResult) {
            is YandexAuthResult.Success -> cloudAuthenticatorCallbacks.onCloudAuthSuccess(authResult.token.value)
            is YandexAuthResult.Failure -> cloudAuthenticatorCallbacks.onCloudAuthFailed(authResult.exception)
            else -> cloudAuthenticatorCallbacks.onCloudAuthCancelled()
        }
    }


    private fun convertLoginType(loginType: LoginType): YandexLoginType {
        return when(loginType) {
            LoginType.WEBVIEW -> YandexLoginType.WEBVIEW
            LoginType.NATIVE -> YandexLoginType.NATIVE
        }
    }
}