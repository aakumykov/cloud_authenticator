package com.github.aakumykov.yandex_authenticator

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdkContract
import com.yandex.authsdk.internal.strategy.LoginType

typealias YandexLoginType = LoginType

class YandexAuthenticator(
    loginType: LoginType = LoginType.NATIVE,
    private val context: Context,
    private val cloudAuthenticatorCallbacks: Callbacks,
    enableLogging: Boolean = false
)
    : CloudAuthenticator()
{
    val yandexAuthOptions: YandexAuthLoginOptions
    val yandexAuthSdkContract: YandexAuthSdkContract

    init {
        yandexAuthOptions = YandexAuthLoginOptions(convertLoginType(loginType))
        yandexAuthSdkContract = YandexAuthSdkContract(YandexAuthOptions(context, enableLogging))
    }

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun prepare(componentActivity: ComponentActivity) {
        activityResultLauncher = componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            processAuthResult(result.resultCode, result.data)
        }
    }

    override fun prepare(fragment: Fragment) {
        activityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            processAuthResult(result.resultCode, result.data)
        }
    }

    override fun startAuth() {
        activityResultLauncher?.launch(
            yandexAuthSdkContract.createIntent(context,yandexAuthOptions)
        )
    }

    override fun deAuth() {
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }



    private fun processAuthResult(resultCode: Int, data: Intent?) {
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