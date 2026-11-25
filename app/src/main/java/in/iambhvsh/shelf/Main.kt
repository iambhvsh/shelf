package `in`.iambhvsh.shelf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import `in`.iambhvsh.shelf.screens.root.Root
import `in`.iambhvsh.shelf.theme.ShelfTheme
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import org.koin.android.ext.android.inject
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : FragmentActivity() {
    private val settingsRepository: SettingsRepository by inject()
    private var currentIntent by mutableStateOf<Intent?>(null)
    private var intentId by mutableIntStateOf(0)

    private lateinit var biometricPrompt: BiometricPrompt
    private var promptInfo: BiometricPrompt.PromptInfo? = null
    private var isAuthenticating by mutableStateOf(false)
    private var isAuthenticatedState by mutableStateOf(true)
    private var biometricError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        currentIntent = intent

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticatedState = true
                    SessionManager.isUnlocked = true
                    isAuthenticating = false
                    biometricError = null
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isAuthenticating = false
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        finish()
                    } else {
                        biometricError = errString.toString().ifBlank { "Authentication failed" }
                    }
                }
            }
        )

        val appLockEnabled = settingsRepository.getAppLockEnabled()
        val usePin = settingsRepository.getAppLockUsePinEnabled()
        isAuthenticatedState = !SessionManager.shouldRequireAuthentication(
            appLockEnabled,
            settingsRepository.getAppLockTimeout()
        )
        if (appLockEnabled) {
            promptInfo = buildPromptInfoIfAvailable(usePin)
        }

        setContent {
            val sharedUrl = when (currentIntent?.action) {
                Intent.ACTION_SEND -> currentIntent?.getStringExtra(Intent.EXTRA_TEXT)?.trim()
                else -> null
            }
            val openBookmarkId = currentIntent?.getLongExtra("OPEN_BOOKMARK_ID", -1L)?.takeIf { it != -1L }

            if (isAuthenticatedState) {
                Root(sharedUrl = sharedUrl, openBookmarkId = openBookmarkId, intentId = intentId)
            } else {
                ShelfTheme(
                    themeMode = settingsRepository.getThemeMode(),
                    dynamicColor = settingsRepository.getDynamicColor(),
                    accentColor = settingsRepository.getAccentColor()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Shelf is locked",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            val errorText = biometricError
                            if (!errorText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (!isAuthenticating) {
                                Spacer(modifier = Modifier.height(24.dp))
                                ExpressiveButton(onClick = { triggerAuthentication() }) {
                                    Text("Unlock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appLockEnabled = settingsRepository.getAppLockEnabled()
        if (appLockEnabled) {
            val requiresAuth = SessionManager.shouldRequireAuthentication(
                appLockEnabled,
                settingsRepository.getAppLockTimeout()
            )
            isAuthenticatedState = !requiresAuth

            if (requiresAuth) {
                val info = buildPromptInfoIfAvailable(settingsRepository.getAppLockUsePinEnabled())
                promptInfo = info
                if (info != null && !isAuthenticating && biometricError == null) {
                    isAuthenticating = true
                    biometricPrompt.authenticate(info)
                }
            }
        } else {
            isAuthenticatedState = true
            SessionManager.isUnlocked = true
            biometricError = null
        }
    }

    private fun triggerAuthentication() {
        biometricError = null
        val info = buildPromptInfoIfAvailable(settingsRepository.getAppLockUsePinEnabled()) ?: return
        promptInfo = info
        if (!isAuthenticating) {
            isAuthenticating = true
            biometricPrompt.authenticate(info)
        }
    }

    private fun buildPromptInfoIfAvailable(usePin: Boolean): BiometricPrompt.PromptInfo? {
        val authenticators = if (usePin) {
            BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG or BIOMETRIC_WEAK
        }

        val canAuthenticate = BiometricManager.from(this).canAuthenticate(authenticators)
        if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
            canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
            canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
        ) {
            settingsRepository.setAppLockEnabled(false)
            isAuthenticatedState = true
            SessionManager.isUnlocked = true
            biometricError = null
            return null
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Shelf")
            .setSubtitle(
                if (usePin) "Use your fingerprint or PIN to access bookmarks"
                else "Use your fingerprint to access bookmarks"
            )
            .setAllowedAuthenticators(authenticators)

        if (!usePin) {
            promptInfoBuilder.setNegativeButtonText("Cancel")
        }

        return promptInfoBuilder.build()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
        intentId++
    }
}

internal fun isHttpOrHttpsUrl(url: String): Boolean {
    val trimmed = url.trim()
    return trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
}

fun openChromeTab(url: String, context: Context) {
    val trimmed = url.trim()
    if (!isHttpOrHttpsUrl(trimmed)) {
        Toast.makeText(context, "Unsupported link", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = trimmed.toUri()
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    } catch (_: Exception) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }
}
