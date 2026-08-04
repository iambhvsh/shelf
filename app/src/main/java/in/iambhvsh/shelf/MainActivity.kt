package `in`.iambhvsh.shelf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import `in`.iambhvsh.shelf.presentation.root.RootScreen

import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import org.koin.android.ext.android.inject
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

class MainActivity : FragmentActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            else -> null
        }
        
        val openBookmarkId = intent?.getLongExtra("OPEN_BOOKMARK_ID", -1L)?.takeIf { it != -1L }

        val appLockEnabled = settingsRepository.getAppLockEnabled()
        val usePin = settingsRepository.getAppLockUsePinEnabled()
        var isAuthenticated by mutableStateOf(!appLockEnabled)

        if (appLockEnabled) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isAuthenticated = true
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Could finish() here if we want to force exit on error/cancel
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                            finish()
                        }
                    }
                })

            val authenticators = if (usePin) {
                BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            } else {
                BIOMETRIC_STRONG or BIOMETRIC_WEAK
            }

            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Shelf")
                .setSubtitle(if (usePin) "Use your fingerprint or PIN to access bookmarks" else "Use your fingerprint to access bookmarks")
                .setAllowedAuthenticators(authenticators)

            if (!usePin) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            }

            biometricPrompt.authenticate(promptInfoBuilder.build())
        }

        setContent {
            if (isAuthenticated) {
                RootScreen(sharedUrl = sharedUrl, openBookmarkId = openBookmarkId)
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

fun openChromeTab(url: String, context: Context) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(context, url.toUri())
}
