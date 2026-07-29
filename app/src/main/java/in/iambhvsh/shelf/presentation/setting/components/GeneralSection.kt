package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.presentation.setting.TapAction
import `in`.iambhvsh.shelf.presentation.setting.ViewMode

@Composable
fun GeneralSection(state: SettingState, viewModel: SettingViewModel) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("General")
    SettingItem(
        icon = Icons.Outlined.TouchApp,
        title = "Default tap action",
        subtitle = when (state.tapAction) {
            TapAction.SHOW_PREVIEW -> "Preview"
            TapAction.OPEN_BROWSER -> "Open in browser"
            TapAction.COPY_LINK -> "Copy link"
        },
        onClick = { viewModel.onEvent(SettingEvents.ShowTapActionSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = if (state.viewMode == ViewMode.GRID) Icons.Outlined.GridView else Icons.Outlined.ViewList,
        title = "View mode",
        subtitle = if (state.viewMode == ViewMode.GRID) "Grid" else "List",
        onClick = { viewModel.onEvent(SettingEvents.ShowViewModeSheet) }
    )
}
