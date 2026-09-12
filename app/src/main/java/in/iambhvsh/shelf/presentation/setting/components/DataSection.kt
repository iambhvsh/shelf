package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel

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

    Spacer(Modifier.height(12.dp))
    SectionHeader("Data")
    SettingItem(
        icon = Icons.Outlined.Upload,
        title = "Export bookmarks",
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowExportSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = Icons.Outlined.Download,
        title = "Import bookmarks",
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowImportSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = Icons.Outlined.Backup,
        title = "Auto backup",
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
    AnimatedVisibility(visible = state.autoBackupEnabled) {
        Column {
            Spacer(Modifier.height(4.dp))
            
            val folderName = if (state.autoBackupUri != null) {
                try {
                    val uri = Uri.parse(state.autoBackupUri)
                    DocumentFile.fromTreeUri(context, uri)?.name ?: "Unknown Folder"
                } catch (e: Exception) {
                    "Unknown Folder"
                }
            } else {
                "Downloads / Shelf"
            }
            
            SettingItem(
                icon = Icons.Outlined.Folder,
                title = "Backup folder",
                subtitle = folderName,
                onClick = { backupDirLauncher.launch(null) }
            )
        }
    }
}
