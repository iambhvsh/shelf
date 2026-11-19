package `in`.iambhvsh.shelf.presentation.setting.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

private var cachedFolderUri: String? = null
private var cachedFolderName: String? = null

@Composable
fun DataSection(
    state: SettingState,
    viewModel: SettingViewModel
) {
    val context = LocalContext.current
    val backupDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                viewModel.onEvent(SettingEvents.SetAutoBackupUri(uri.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Spacer(Modifier.height(ShelfShapes.Gap * 4))
    SectionHeader("Data")
    val count = if (state.autoBackupEnabled) 4 else 3

    SettingItem(
        icon = Icons.Outlined.Upload,
        title = "Export bookmarks",
        index = 0,
        count = count,
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowExportSheet) }
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.Download,
        title = "Import bookmarks",
        index = 1,
        count = count,
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowImportSheet) }
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.Backup,
        title = "Auto backup",
        index = 2,
        count = count,
        subtitle = if (state.autoBackupEnabled) {
            val time = state.lastBackupTimeText
            if (time.isNotEmpty()) time
            else "No backup yet"
        } else "Off",
        trailing = {
            Switch(
                checked = state.autoBackupEnabled,
                onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        onClick = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(!state.autoBackupEnabled)) }
    )
    if (state.autoBackupEnabled) {
        Spacer(Modifier.height(ShelfShapes.Gap))

        val folderName = remember(state.autoBackupUri) {
            if (state.autoBackupUri != null) {
                if (state.autoBackupUri == cachedFolderUri && cachedFolderName != null) {
                    cachedFolderName!!
                } else {
                    try {
                        val uri = Uri.parse(state.autoBackupUri)
                        (DocumentFile.fromTreeUri(context, uri)?.name ?: "Unknown Folder").also {
                            cachedFolderUri = state.autoBackupUri
                            cachedFolderName = it
                        }
                    } catch (e: Exception) {
                        "Unknown Folder"
                    }
                }
            } else {
                "Downloads / Shelf"
            }
        }

        SettingItem(
            icon = Icons.Outlined.Folder,
            title = "Backup folder",
            index = 3,
            count = count,
            subtitle = folderName,
            onClick = { backupDirLauncher.launch(null) }
        )
    }
}
