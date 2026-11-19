@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.iambhvsh.shelf.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import `in`.iambhvsh.shelf.home.components.UpdateSheet
import `in`.iambhvsh.shelf.setting.components.AboutSheet
import `in`.iambhvsh.shelf.setting.components.AccentColorSheet
import `in`.iambhvsh.shelf.setting.components.ChangelogSheet
import `in`.iambhvsh.shelf.setting.components.OptionSheet
import `in`.iambhvsh.shelf.setting.components.RadioOptionSheet
import `in`.iambhvsh.shelf.ui.theme.ThemeMode

@Composable
fun SettingDialogsAndSheets(
    state: SettingState,
    viewModel: SettingViewModel,
    versionName: String,
    context: Context
) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val json = (state.exportState as? ExportState.Ready)?.json
            if (json != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
        viewModel.onEvent(SettingEvents.DismissExport)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            if (json.isNotBlank()) viewModel.onEvent(SettingEvents.ImportData(json))
        }
    }

    val importBrowserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            val html = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            if (html.isNotBlank()) viewModel.onEvent(SettingEvents.ImportBrowserBookmarks(html))
        }
    }

    val exportHtmlLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/html")
    ) { uri: Uri? ->
        if (uri != null) {
            val html = (state.browserExportState as? ExportState.Ready)?.json
            if (html != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(html.toByteArray()) }
            }
        }
        viewModel.onEvent(SettingEvents.DismissBrowserExport)
    }

    LaunchedEffect(state.exportState) {
        if (state.exportState is ExportState.Ready) {
            exportLauncher.launch("shelf_backup.json")
        }
    }

    LaunchedEffect(state.browserExportState) {
        if (state.browserExportState is ExportState.Ready) {
            exportHtmlLauncher.launch("shelf_bookmarks.html")
        }
    }

    SettingDialogs(state = state, viewModel = viewModel, context = context)

    if (state.showThemeSheet) {
        RadioOptionSheet(
            title = "Choose Theme",
            options = listOf(
                "Light" to ThemeMode.LIGHT,
                "Dark" to ThemeMode.DARK,
                "Oled" to ThemeMode.OLED,
                "System default" to ThemeMode.SYSTEM
            ),
            current = state.themeMode,
            onSelect = { viewModel.onEvent(SettingEvents.SelectTheme(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideThemeSheet) }
        )
    }

    if (state.showAppLockTimeoutSheet) {
        RadioOptionSheet(
            title = "Lock After",
            options = listOf(
                "Immediately" to 0L,
                "1 minute" to 60_000L,
                "5 minutes" to 300_000L,
                "15 minutes" to 900_000L
            ),
            current = state.appLockTimeout,
            onSelect = { viewModel.onEvent(SettingEvents.SetAppLockTimeout(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideAppLockTimeoutSheet) }
        )
    }

    if (state.showAccentColorSheet) {
        AccentColorSheet(
            current = state.accentColor,
            onSelect = { viewModel.onEvent(SettingEvents.SelectAccentColor(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideAccentColorSheet) }
        )
    }

    if (state.showTapActionSheet) {
        RadioOptionSheet(
            title = "On tap",
            options = listOf(
                "Show preview" to TapAction.SHOW_PREVIEW,
                "Open in browser" to TapAction.OPEN_BROWSER,
                "Copy link" to TapAction.COPY_LINK
            ),
            current = state.tapAction,
            onSelect = { viewModel.onEvent(SettingEvents.SelectTapAction(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideTapActionSheet) }
        )
    }

    if (state.showViewModeSheet) {
        RadioOptionSheet(
            title = "View mode",
            options = listOf(
                "Grid" to ViewMode.GRID,
                "List" to ViewMode.LIST
            ),
            current = state.viewMode,
            onSelect = { viewModel.onEvent(SettingEvents.ToggleViewMode(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideViewModeSheet) }
        )
    }

    if (state.showExportSheet) {
        OptionSheet(
            title = "Export as",
            options = listOf(
                "JSON" to {
                    viewModel.onEvent(SettingEvents.HideExportSheet)
                    viewModel.onEvent(SettingEvents.ExportData)
                },
                "HTML" to {
                    viewModel.onEvent(SettingEvents.HideExportSheet)
                    viewModel.onEvent(SettingEvents.ExportBrowserBookmarks)
                }
            ),
            onDismiss = { viewModel.onEvent(SettingEvents.HideExportSheet) }
        )
    }

    if (state.showImportSheet) {
        OptionSheet(
            title = "Import from",
            options = listOf(
                "JSON" to {
                    viewModel.onEvent(SettingEvents.HideImportSheet)
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, DocumentsContract.buildDocumentUri(
                                "com.android.externalstorage.documents",
                                "primary:Download/Shelf"
                            ))
                        }
                    }
                    importLauncher.launch(intent)
                },
                "HTML" to {
                    viewModel.onEvent(SettingEvents.HideImportSheet)
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/html"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/html", "text/plain"))
                    }
                    importBrowserLauncher.launch(intent)
                }
            ),
            onDismiss = { viewModel.onEvent(SettingEvents.HideImportSheet) }
        )
    }

    if (state.showChangelogSheet) {
        ChangelogSheet(
            changelogText = state.changelogText,
            onDismiss = { viewModel.onEvent(SettingEvents.HideChangelogSheet) }
        )
    }

    if (state.showAboutSheet) {
        AboutSheet(
            versionName = versionName,
            onDismiss = { viewModel.onEvent(SettingEvents.HideAboutSheet) }
        )
    }

    if (state.showUpdateSheet) {
        UpdateSheet(
            onDismiss = { viewModel.onEvent(SettingEvents.DismissUpdateSheet) },
            onInstall = { viewModel.onEvent(SettingEvents.InstallUpdate) }
        )
    }
}
