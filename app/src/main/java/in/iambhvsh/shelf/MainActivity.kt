package `in`.iambhvsh.shelf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import `in`.iambhvsh.shelf.presentation.root.RootScreen

import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import org.koin.android.ext.android.inject
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

class MainActivity : FragmentActivity() {
    private val settingsRepository: SettingsRepository by inject()
    private var currentIntent by mutableStateOf<Intent?>(null)
    private var intentId by mutableIntStateOf(0)
    
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var isAuthenticating = false
    private var isAuthenticatedState by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        currentIntent = intent

        val appLockEnabled = settingsRepository.getAppLockEnabled()
        val usePin = settingsRepository.getAppLockUsePinEnabled()
        
        isAuthenticatedState = !SessionManager.shouldRequireAuthentication(
            appLockEnabled, settingsRepository.getAppLockTimeout()
        )

        if (appLockEnabled) {
            val authenticators = if (usePin) {
                BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
            } else {
                BIOMETRIC_STRONG or BIOMETRIC_WEAK
            }

            val biometricManager = BiometricManager.from(this)
            val canAuthenticate = biometricManager.canAuthenticate(authenticators)

            if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
                canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
                
                settingsRepository.setAppLockEnabled(false)
                isAuthenticatedState = true
                SessionManager.isUnlocked = true
            } else {
                val executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            isAuthenticatedState = true
                            SessionManager.isUnlocked = true
                            isAuthenticating = false
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            isAuthenticating = false
                            if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                                finish()
                            }
                        }
                    })

                val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Shelf")
                    .setSubtitle(if (usePin) "Use your fingerprint or PIN to access bookmarks" else "Use your fingerprint to access bookmarks")
                    .setAllowedAuthenticators(authenticators)

                if (!usePin) {
                    promptInfoBuilder.setNegativeButtonText("Cancel")
                }

                promptInfo = promptInfoBuilder.build()
            }
        }

        setContent {
            val sharedUrl = when (currentIntent?.action) {
                Intent.ACTION_SEND -> currentIntent?.getStringExtra(Intent.EXTRA_TEXT)?.trim()
                else -> null
            }
            val openBookmarkId = currentIntent?.getLongExtra("OPEN_BOOKMARK_ID", -1L)?.takeIf { it != -1L }

            if (isAuthenticatedState) {
                RootScreen(sharedUrl = sharedUrl, openBookmarkId = openBookmarkId, intentId = intentId)
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appLockEnabled = settingsRepository.getAppLockEnabled()
        if (appLockEnabled) {
            val requiresAuth = SessionManager.shouldRequireAuthentication(
                appLockEnabled, settingsRepository.getAppLockTimeout()
            )
            isAuthenticatedState = !requiresAuth

            if (requiresAuth && !isAuthenticating && ::biometricPrompt.isInitialized) {
                isAuthenticating = true
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            isAuthenticatedState = true
            SessionManager.isUnlocked = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
        intentId++
    }
}

fun openChromeTab(url: String, context: Context) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(context, url.toUri())
}
