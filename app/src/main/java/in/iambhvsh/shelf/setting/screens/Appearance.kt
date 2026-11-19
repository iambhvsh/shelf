@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.setting.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.setting.SettingEvents
import `in`.iambhvsh.shelf.setting.SettingState
import `in`.iambhvsh.shelf.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes
import `in`.iambhvsh.shelf.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

    val hasAccent = !state.isDynamicColorSupported || !state.dynamicColor
    val count = if (state.isDynamicColorSupported) {
        if (hasAccent) 3 else 2
    } else 2

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding)
    ) {
        // Theme item
        item(key = "theme") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ShowThemeSheet) },
                shapes = ShelfShapes.segmented(0, count),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = when (state.themeMode) {
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                            ThemeMode.OLED -> "Oled"
                            ThemeMode.SYSTEM -> "System default"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        // Dynamic color item
        if (state.isDynamicColorSupported) {
            item(key = "dynamic_color") {
                SegmentedListItem(
                    onClick = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(!state.dynamicColor)) },
                    shapes = ShelfShapes.segmented(1, count),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (state.dynamicColor) "Match wallpaper colors" else "Use custom accent color",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = state.dynamicColor,
                            onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    },
                    content = {
                        Text(
                            text = "Dynamic color",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

        // Accent color item
        if (hasAccent) {
            item(key = "accent_color") {
                val index = if (state.isDynamicColorSupported) 2 else 1
                SegmentedListItem(
                    onClick = { viewModel.onEvent(SettingEvents.ShowAccentColorSheet) },
                    shapes = ShelfShapes.segmented(index, count),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    supportingContent = {
                        Text(
                            text = state.accentColor.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(state.accentColor.seed)
                        )
                    },
                    content = {
                        Text(
                            text = "Accent color",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}
