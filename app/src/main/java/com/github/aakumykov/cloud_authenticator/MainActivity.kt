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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.aakumykov.activities.QwertyActivity
import com.github.aakumykov.cloud_authenticator.databinding.ActivityMainBinding
import com.github.aakumykov.activities.extensions.errorMsg
import com.github.aakumykov.activities.extensions.errorMsgExtended
import com.github.aakumykov.google_authenticator.GoogleAuthenticator
import com.github.aakumykov.activities.CloudAuthProvider
import com.github.aakumykov.kotlin_playground.extensions.eraseStringFromPreferences
import com.github.aakumykov.kotlin_playground.extensions.getStringFromPreferences
import com.github.aakumykov.kotlin_playground.extensions.makeGone
import com.github.aakumykov.kotlin_playground.extensions.makeVisible
import com.github.aakumykov.kotlin_playground.extensions.showToast
import com.github.aakumykov.kotlin_playground.extensions.storeStringInPreferences
import com.github.aakumykov.yandex_authenticator.YandexAuthenticator
import com.google.android.material.button.MaterialButton

class MainActivity : QwertyActivity() {

}