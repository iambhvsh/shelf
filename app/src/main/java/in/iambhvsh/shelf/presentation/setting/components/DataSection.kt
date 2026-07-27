package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel

@Composable
fun DataSection(
    state: SettingState,
    viewModel: SettingViewModel
) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Data")
    SettingItem(
        icon = R.drawable.backup_db,
        title = "Export bookmarks",
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowExportSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.import_icon,
        title = "Import bookmarks",
        subtitle = "JSON or HTML",
        onClick = { viewModel.onEvent(SettingEvents.ShowImportSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.backup_icon,
        title = "Auto backup",
        subtitle = if (state.autoBackupEnabled) {
            val time = state.lastBackupTimeText
            if (time.isNotEmpty()) "$time\n/storage/Download/Shelf/shelf_autobackup.json"
            else "No backup yet\n/storage/Download/Shelf/shelf_autobackup.json"
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
}
