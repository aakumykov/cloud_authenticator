package com.github.aakumykov.google_authenticator

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.aakumykov.cloud_authenticator.CloudAuthenticator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleAuthenticator(
    loginType: LoginType = LoginType.NATIVE,
    private val context: Context,
    private val cloudAuthenticatorCallbacks: Callbacks,
)
    : CloudAuthenticator()
{
    private lateinit var googleSignInOptions: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    override fun startAuth(context: Context) {
        prepareGoogleSignInStuff(context)
        activityResultLauncher.launch(googleSignInClient.signInIntent)
    }

    override fun prepare(
        componentActivity: ComponentActivity,
        loginType: LoginType,
        enableLogging: Boolean
    ) {
        prepareGoogleSignInStuff(componentActivity)

        activityResultLauncher = componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            parseResult(activityResult)
        }
    }

    override fun prepare(
        fragment: Fragment,
        loginType: LoginType,
        enableLogging: Boolean
    ) {
        prepareGoogleSignInStuff(fragment.requireContext())

        activityResultLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            parseResult(activityResult)
        }
    }

    override fun prepare(context: Context,
                        loginType: LoginType,
                        activityResultLauncher: ActivityResultLauncher<Intent>,
                        enableLogging: Boolean
    ) {
        this.activityResultLauncher = activityResultLauncher
    }

    override fun parseResult(activityResult: ActivityResult) {
        when(activityResult.resultCode) {
            RESULT_OK -> processSignInData(activityResult.data)
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

    private fun prepareGoogleSignInStuff(context: Context) {
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestEmail()
            .requestIdToken(GOOGLE_AUTH_PLATFORM_CLIENT_ID)
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
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