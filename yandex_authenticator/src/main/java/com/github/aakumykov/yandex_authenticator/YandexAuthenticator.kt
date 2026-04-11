package com.github.aakumykov.yandex_authenticator

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdkContract

typealias YandexLoginType = com.yandex.authsdk.internal.strategy.LoginType

class YandexAuthenticator(private val cloudAuthenticatorCallbacks: Callbacks, )
    : CloudAuthenticator()
{
    private lateinit var yandexAuthOptions: YandexAuthOptions
    private lateinit var yandexAuthLoginOptions: YandexAuthLoginOptions
    private lateinit var yandexAuthContract: YandexAuthSdkContract

    private val activityResultContract: ActivityResultContracts.StartActivityForResult by lazy {
        ActivityResultContracts.StartActivityForResult()
    }

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    override fun prepare(
        componentActivity: ComponentActivity,
        loginType: LoginType,
        enableLogging: Boolean
    ) {
        prepareAuthenticatorStuff(componentActivity, loginType, enableLogging)

        activityResultLauncher = componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            parseResult(activityResult)
        }
    }

    override fun prepare(
        fragment: Fragment,
        loginType: LoginType,
        enableLogging: Boolean
    ) {
        prepareAuthenticatorStuff(fragment.requireContext(), loginType, enableLogging)

        activityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            parseResult(activityResult)
        }
    }

    override fun startAuth(context: Context) {
        val intent = yandexAuthContract.createIntent(context, yandexAuthLoginOptions)
        activityResultLauncher.launch(activityResultContract.createIntent(context, intent))
    }

    override fun prepare(context: Context,
                         loginType: LoginType,
                         activityResultLauncher: ActivityResultLauncher<Intent>,
                         enableLogging: Boolean) {
        this.activityResultLauncher = activityResultLauncher
        prepareAuthenticatorStuff(context, loginType, enableLogging)
    }

    override fun parseResult(activityResult: ActivityResult) {
        val yandexAuthResult: YandexAuthResult = yandexAuthContract.parseResult(activityResult.resultCode, activityResult.data)
        when(yandexAuthResult) {
            is YandexAuthResult.Success -> cloudAuthenticatorCallbacks.onCloudAuthSuccess(yandexAuthResult.token.value)
            is YandexAuthResult.Failure -> cloudAuthenticatorCallbacks.onCloudAuthFailed(yandexAuthResult.exception)
            is YandexAuthResult.Cancelled -> cloudAuthenticatorCallbacks.onCloudAuthCancelled()
        }
    }

    override fun deAuth() {
        cloudAuthenticatorCallbacks.onDeAuthSuccess()
    }

    private fun prepareAuthenticatorStuff(
        context: Context,
        loginType: LoginType,
        enableLogging: Boolean
    ) {
        yandexAuthLoginOptions = yandexAuthLoginOptions(loginType)
        yandexAuthOptions = yandexAuthOptions(context, enableLogging)
        yandexAuthContract = yandexAuthSdkContract(yandexAuthOptions)
    }

    private fun yandexAuthSdkContract(yandexAuthOptions: YandexAuthOptions): YandexAuthSdkContract {
        return YandexAuthSdkContract(yandexAuthOptions)
    }

    private fun yandexAuthLoginOptions(loginType: LoginType): YandexAuthLoginOptions {
        return YandexAuthLoginOptions(convertLoginType(loginType))
    }

    private fun yandexAuthOptions(
        context: Context,
        enableLogging: Boolean
    ): YandexAuthOptions {
        return YandexAuthOptions(context, enableLogging)
    }

    private fun convertLoginType(loginType: LoginType): YandexLoginType {
        return when(loginType) {
            LoginType.NATIVE -> YandexLoginType.NATIVE
            else -> YandexLoginType.WEBVIEW
        }
    }
}