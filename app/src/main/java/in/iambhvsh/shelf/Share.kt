package `in`.iambhvsh.shelf

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import `in`.iambhvsh.shelf.features.save.QuickSaveEvents
import `in`.iambhvsh.shelf.screens.save.QuickSaveScreen
import `in`.iambhvsh.shelf.features.save.QuickSaveViewModel
import `in`.iambhvsh.shelf.theme.ShelfTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class QuickSaveActivity : ComponentActivity() {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.canAccessQuickSave(
                appLockEnabled = settingsRepository.getAppLockEnabled(),
                timeout = settingsRepository.getAppLockTimeout()
            )
        ) {
            Toast.makeText(this, "Shelf is locked. Unlock Shelf first.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        
        if (sharedText.isNullOrEmpty()) {
            Toast.makeText(this, "No valid link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val url = UrlUtils.extractUrl(sharedText)
        if (url.isBlank()) {
            Toast.makeText(this, "No valid link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            val viewModel: QuickSaveViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            androidx.compose.runtime.LaunchedEffect(url) {
                viewModel.onEvent(QuickSaveEvents.Init(url))
            }

            ShelfTheme(
                themeMode = settingsRepository.getThemeMode(),
                dynamicColor = settingsRepository.getDynamicColor(),
                accentColor = settingsRepository.getAccentColor()
            ) {
                QuickSaveScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
