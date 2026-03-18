package com.github.aakumykov.google_authenticator

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleAuthenticator(
    loginType: LoginType,
    private val context: Context,
    private val cloudAuthenticatorCallbacks: Callbacks,
)
    : CloudAuthenticator()
{
    private val googleSignInOptions: GoogleSignInOptions
    private val googleSignInClient: GoogleSignInClient

    init {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestEmail()
            .requestIdToken(GOOGLE_AUTH_PLATFORM_CLIENT_ID)
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    }


    override fun startAuth(activityResultLauncher: ActivityResultLauncher<Intent>) {
        activityResultLauncher.launch(googleSignInClient.signInIntent)
    }


    override fun processAuthResult(resultCode: Int, data: Intent?) {
        when(resultCode) {
            RESULT_OK -> processSignInData(data)
            RESULT_CANCELED -> cloudAuthenticatorCallbacks.onCloudAuthCancelled()
            else -> cloudAuthenticatorCallbacks.onCloudAuthFailed(Exception("Unknown result"))
        }
    }


    override fun deAuth() {
        googleSignInClient.signOut()
            .addOnSuccessListener { cloudAuthenticatorCallbacks.onDeAuthSuccess() }
            .addOnCanceledListener { cloudAuthenticatorCallbacks.onDeAuthCancelled() }
            .addOnFailureListener { exception -> cloudAuthenticatorCallbacks.onDeAuthError(exception) }
    }


    private fun processSignInData(data: Intent?) {
        try {
            GoogleSignIn
                .getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)

            processGoogleAccount()

        } catch (e: ApiException) {
            Log.e(TAG, e.message, e)
            cloudAuthenticatorCallbacks.onCloudAuthFailed(e)
        }
    }


    private fun processGoogleAccount() {

        val account: GoogleSignInAccount? = GoogleSignIn
            .getLastSignedInAccount(context)

        if (null == account) {
            Exception("Error getting account info").also {
                Log.e(TAG, it.message, it)
                cloudAuthenticatorCallbacks.onCloudAuthFailed(it)
            }
            return
        }

        val authToken: String? = account.idToken

        if (null == authToken) {
            cloudAuthenticatorCallbacks.onCloudAuthFailed(
                Exception("Id token from account is null")
            )
            return
        }

        cloudAuthenticatorCallbacks.onCloudAuthSuccess(authToken)
    }


    companion object {
        val TAG: String = GoogleAuthenticator::class.java.simpleName
    }
}