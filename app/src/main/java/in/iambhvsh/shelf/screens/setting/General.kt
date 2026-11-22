@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsGeneralScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
    ) {
        SegmentedListItem(
            onClick = { viewModel.onEvent(SettingEvents.ShowTapActionSheet) },
            shapes = ShelfShapes.segmented(0, 3),
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.segmentedColors(
                containerColor = containerColor
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            supportingContent = {
                Text(
                    text = when (state.tapAction) {
                        TapAction.SHOW_PREVIEW -> "Preview"
                        TapAction.OPEN_BROWSER -> "Open in browser"
                        TapAction.COPY_LINK -> "Copy link"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            content = {
                Text(
                    text = "Default tap action",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        SegmentedListItem(
            onClick = { viewModel.onEvent(SettingEvents.ShowViewModeSheet) },
            shapes = ShelfShapes.segmented(1, 3),
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.segmentedColors(
                containerColor = containerColor
            ),
            leadingContent = {
                Icon(
                    imageVector = if (state.viewMode == ViewMode.GRID) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            supportingContent = {
                Text(
                    text = if (state.viewMode == ViewMode.GRID) "Grid" else "List",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            content = {
                Text(
                    text = "View mode",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        SegmentedListItem(
            onClick = { viewModel.onEvent(SettingEvents.ShowPermissionsSheet) },
            shapes = ShelfShapes.segmented(2, 3),
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.segmentedColors(
                containerColor = containerColor
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            supportingContent = {
                Text(
                    text = "Notifications, alarms & battery settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            content = {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}
