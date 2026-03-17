package com.github.aakumykov.cloud_authenticator

abstract class CloudAuthenticator() {

    abstract fun startAuth()
    abstract fun deAuth()

    interface Callbacks {
        fun onCloudAuthSuccess(authToken: String)
        fun onCloudAuthFailed(throwable: Throwable)
        fun onCloudAuthCancelled()

        fun onDeAuthSuccess() {}
        fun onDeAuthCancelled(message: String? = null) {}
        fun onDeAuthError(throwable: Throwable) {}
    }

    enum class LoginType {
        NATIVE,
        WEBVIEW
    }
}