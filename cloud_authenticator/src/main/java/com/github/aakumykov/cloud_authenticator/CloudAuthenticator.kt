package com.github.aakumykov.cloud_authenticator

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

/**
 * Интерфейс предлагает два варианта работы:
 *
 * 1) с внутренним [ActivityResultLauncher]
 * Удобно тем, что не нужно заранее готовить [ActivityResultLauncher]<Intent>;
 * неудобство - нельзя использовать динамическое создание "аутентификаторов",
 * так как его метод [prepare] нужно вызывать на ранней стадии жизненного цикла
 * Activity/Fragment: до onResume().
 *
 * 2) с внешним [ActivityResultLauncher]<Intent>.
 * Позволяет создавать "аутентификаторы" в рантайме, но требует
 * заранее подготовленного [ActivityResultLauncher]<Intent>,
 * который передаётся в метод [prepare]; в этом случае [prepare]
 * можно вызывать позже onResume().
 */
abstract class CloudAuthenticator() {

    /**
     * Используется в связке с [startAuth].
     */
    abstract fun prepare(componentActivity: ComponentActivity,
                         loginType: LoginType = LoginType.NATIVE,
                         enableLogging: Boolean = false)

    abstract fun prepare(fragment: Fragment,
                         loginType: LoginType = LoginType.NATIVE,
                         enableLogging: Boolean = false)
    /**
     * Используется в связке с [prepare] (componentActivity: ComponentActivity,
     *                          loginType: LoginType, enableLogging: Boolean).
     */
    abstract fun startAuth(context: Context)


    /**
     * Используется в связке с [parseResult].
     */
    abstract fun prepare(
        context: Context,
        loginType: LoginType = LoginType.NATIVE,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        enableLogging: Boolean = false,
    )

    /**
     * Используется в связке с [prepare] (
     *         context: Context,
     *         loginType: LoginType,
     *         activityResultLauncher: ActivityResultLauncher<Intent>,
     *         enableLogging: Boolean = false,
     *     )
     */
    abstract fun parseResult(activityResult: ActivityResult)

    abstract fun deAuth()

    interface Callbacks {
        fun onCloudAuthSuccess(authToken: String) {}
        fun onCloudAuthFailed(throwable: Throwable) {}
        fun onCloudAuthCancelled() {}

        fun onDeAuthSuccess() {}
        fun onDeAuthCancelled(message: String? = null) {}
        fun onDeAuthError(throwable: Throwable) {}
    }

    enum class LoginType {
        NATIVE,
        WEBVIEW
    }
}