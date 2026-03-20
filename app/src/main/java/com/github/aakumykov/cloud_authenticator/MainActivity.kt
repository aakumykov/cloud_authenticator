package com.github.aakumykov.cloud_authenticator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.aakumykov.cloud_authenticator.databinding.ActivityMainBinding
import com.github.aakumykov.cloud_authenticator.extensions.errorMsg
import com.github.aakumykov.cloud_authenticator.extensions.errorMsgExtended
import com.github.aakumykov.google_authenticator.GoogleAuthenticator
import com.github.aakumykov.kotlin_playground.CloudAuthProvider
import com.github.aakumykov.kotlin_playground.extensions.eraseStringFromPreferences
import com.github.aakumykov.kotlin_playground.extensions.getStringFromPreferences
import com.github.aakumykov.kotlin_playground.extensions.makeGone
import com.github.aakumykov.kotlin_playground.extensions.makeVisible
import com.github.aakumykov.kotlin_playground.extensions.showToast
import com.github.aakumykov.kotlin_playground.extensions.storeStringInPreferences
import com.github.aakumykov.local_authenticator.LocalAuthenticator
import com.github.aakumykov.yandex_authenticator.YandexAuthenticator
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityMainBinding

//    private var _oldAuthenticator: CloudAuthenticator? = null
    private var currentAuthenticator: CloudAuthenticator? = null

    /*private var _currentAuthProvider: CloudAuthProvider? = null
    private val currentAuthProvider: CloudAuthProvider get() = _currentAuthProvider!!*/

    private val cloudAuthenticator: CloudAuthenticator get() = when(cloudAuthProvider) {
        CloudAuthProvider.YANDEX -> yandexAuthenticator()
        CloudAuthProvider.GOOGLE -> googleAuthenticator()
        CloudAuthProvider.LOCAL -> localAuthenticator()
    }.apply {
        currentAuthenticator = this
    }

    private var authToken: String? = null

    private val cloudAuthProvider: CloudAuthProvider
        get() = when(binding.authProviders.checkedRadioButtonId) {
        R.id.yandexAuth -> CloudAuthProvider.YANDEX
        R.id.googleAuth -> CloudAuthProvider.GOOGLE
        R.id.localAuth -> CloudAuthProvider.LOCAL
        else -> throw IllegalStateException("Неизвестный провайдер авторизации в интерфейсе")
    }

    private val cloudAuthProviderName: String get() = when(cloudAuthProvider) {
        CloudAuthProvider.YANDEX -> getString(R.string.yandex)
        CloudAuthProvider.GOOGLE -> getString(R.string.google)
        CloudAuthProvider.LOCAL -> getString(R.string.local)
    }


    private val authCallbacks = object: CloudAuthenticator.Callbacks {

        override fun onCloudAuthSuccess(authToken: String) {
            this@MainActivity.authToken = authToken
            storeStringInPreferences(AUTH_TOKEN, authToken)
            displayAuthStatus()
            showToast("Авторизация успешна")
        }

        override fun onCloudAuthFailed(throwable: Throwable) {
            showError(throwable)
        }

        override fun onCloudAuthCancelled() {
            showToast("Авторизация Google отменена")
        }

        override fun onDeAuthSuccess() {
            this@MainActivity.authToken = null
            eraseStringFromPreferences(AUTH_TOKEN)
            displayAuthStatus()
            showToast("Выход осуществлён")
        }

        override fun onDeAuthCancelled(message: String?) {
            showToast("Выход отменён: ${message ?: ""}")
        }

        override fun onDeAuthError(throwable: Throwable) {
            showError(throwable)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            cloudAuthenticator.processAuthResult(result.resultCode, result.data)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setTitle(R.string.app_description)

        binding.authButton.setOnClickListener { onStartAuthClicked() }

        binding.authProviders.setOnCheckedChangeListener(::onAuthProviderChecked)

        authToken = getStringFromPreferences(AUTH_TOKEN)

        displayAuthStatus()
    }

    private fun onAuthProviderChecked(groud: RadioGroup, checkedId: Int) {
        currentAuthenticator?.deAuth()
        binding.authButton.text = "Авторизоваться в $cloudAuthProviderName"
    }

    private fun yandexAuthenticator(): CloudAuthenticator {
        return YandexAuthenticator(
            loginType = loginType(),
            context = this,
            cloudAuthenticatorCallbacks = authCallbacks,
            enableLogging = true
        )
    }

    private fun googleAuthenticator(): CloudAuthenticator {
        return GoogleAuthenticator(
            loginType(),
            this,
            authCallbacks
        )
    }

    private fun localAuthenticator(): CloudAuthenticator {
        return LocalAuthenticator(authCallbacks)
    }

    private fun loginType(): CloudAuthenticator.LoginType {
        return when (binding.loginTypeGroup.checkedRadioButtonId) {
            R.id.nativeLoginType -> CloudAuthenticator.LoginType.NATIVE
            R.id.webViewLoginType -> CloudAuthenticator.LoginType.WEBVIEW
            else -> throw IllegalArgumentException("Неизвестный тип авторизации: ${binding.loginTypeGroup.checkedRadioButtonId}")
        }
    }

    private fun displayAuthStatus() {
        if (null == authToken) {
            binding.authButton.text = getString(R.string.auth_in, cloudAuthProviderName)
            binding.authButton.setIconResource(R.drawable.ic_log_in)
            binding.authButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            binding.authTokenView.apply {
                text = ""
                makeGone()
            }
        }
        else {
            binding.authButton.text = getString(R.string.de_auth_from, cloudAuthProviderName)
            binding.authButton.setIconResource(R.drawable.ic_log_out)
            binding.authButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
            binding.authTokenView.apply {
                makeVisible()
                text = getString(R.string.auth_token, authToken)
            }
        }
    }

    private fun onStartAuthClicked() {
        hideError()

        if (null == authToken) cloudAuthenticator.startAuth(signInLauncher)
        else cloudAuthenticator.deAuth()
    }


    private fun showError(t: Throwable) {
        Log.e(TAG, t.errorMsg, t)
        binding.root.post {
            binding.errorView.text = t.errorMsgExtended
            binding.errorView.visibility = View.VISIBLE
        }
        showToast(t.errorMsgExtended)
    }

    private fun hideError() {
        binding.root.post {
            binding.errorView.visibility = View.GONE
        }
    }

    private fun hideProgressBar() {
        binding.root.post {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showAppProperties() {
        val uri = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        if (intent.resolveActivity(packageManager) != null) { startActivity(intent) }
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        const val AUTH_TOKEN = "AUTH_TOKEN"
    }

}