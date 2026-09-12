package `in`.iambhvsh.shelf

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import `in`.iambhvsh.shelf.presentation.quicksave.QuickSaveEvents
import `in`.iambhvsh.shelf.presentation.quicksave.QuickSaveScreen
import `in`.iambhvsh.shelf.presentation.quicksave.QuickSaveViewModel
import `in`.iambhvsh.shelf.ui.theme.ShelfTheme
import org.koin.androidx.compose.koinViewModel

class QuickSaveActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        
        if (sharedText.isNullOrEmpty()) {
            Toast.makeText(this, "No valid link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val url = if (!sharedText.startsWith("http://") && !sharedText.startsWith("https://")) {
            "https://$sharedText"
        } else {
            sharedText
        }

        setContent {
            val viewModel: QuickSaveViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            androidx.compose.runtime.LaunchedEffect(url) {
                viewModel.onEvent(QuickSaveEvents.Init(url))
            }

            ShelfTheme(
                themeMode = `in`.iambhvsh.shelf.ui.theme.ThemeMode.SYSTEM,
                dynamicColor = true,
                accentColor = `in`.iambhvsh.shelf.ui.theme.AccentColor.PERIWINKLE
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
