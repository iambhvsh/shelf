@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.setting.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import `in`.iambhvsh.shelf.setting.SettingEvents
import `in`.iambhvsh.shelf.setting.SettingState
import `in`.iambhvsh.shelf.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsDataScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

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

    val folderName = remember(state.autoBackupUri) {
        if (state.autoBackupUri != null) {
            try {
                val doc = DocumentFile.fromTreeUri(context, Uri.parse(state.autoBackupUri))
                doc?.name ?: state.autoBackupUri
            } catch (e: Exception) {
                state.autoBackupUri
            }
        } else {
            "Select backup folder"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding)
    ) {
        item(key = "export") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ShowExportSheet) },
                shapes = ShelfShapes.segmented(0, 2),
                modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                },
                supportingContent = {
                    Text(text = "Export to JSON or HTML file", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                content = {
                    Text(text = "Export bookmarks", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        item(key = "import") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ShowImportSheet) },
                shapes = ShelfShapes.segmented(1, 2),
                modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                },
                supportingContent = {
                    Text(text = "Import from Shelf JSON or browser HTML", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                content = {
                    Text(text = "Import bookmarks", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        item(key = "spacer_auto_backup") {
            Spacer(Modifier.height(12.dp))
        }

        val autoBackupCount = if (state.autoBackupEnabled) 2 else 1
        item(key = "auto_backup") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(!state.autoBackupEnabled)) },
                shapes = ShelfShapes.segmented(0, autoBackupCount),
                modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                },
                supportingContent = {
                    Text(
                        text = if (state.autoBackupEnabled) {
                            if (state.lastBackupTimeText.isNotEmpty()) state.lastBackupTimeText else "Scheduled daily"
                        } else "Automatically backup daily to storage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Switch(
                        checked = state.autoBackupEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAutoBackup(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                },
                content = {
                    Text(text = "Auto backup", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            )
        }

        if (state.autoBackupEnabled) {
            item(key = "backup_folder") {
                SegmentedListItem(
                    onClick = { backupDirLauncher.launch(null) },
                    shapes = ShelfShapes.segmented(1, 2),
                    modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    leadingContent = {
                        Icon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    },
                    supportingContent = {
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    content = {
                        Text(text = "Backup folder", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                )
            }
        }
    }
}
