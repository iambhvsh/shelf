@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsSecurityScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

    val count = if (state.appLockEnabled) 3 else 1

    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
    ) {
        SegmentedListItem(
            onClick = { viewModel.onEvent(SettingEvents.ToggleAppLock(!state.appLockEnabled)) },
            shapes = ShelfShapes.segmented(0, count),
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.segmentedColors(
                containerColor = containerColor
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            supportingContent = {
                Text(
                    text = if (state.appLockEnabled) "Enabled (Biometrics)" else "Require fingerprint or face to open Shelf",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = state.appLockEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAppLock(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            content = {
                Text(
                    text = "App Lock",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        AnimatedVisibility(
            visible = state.appLockEnabled,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessLow)) +
                    shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)) {
                SegmentedListItem(
                    onClick = { viewModel.onEvent(SettingEvents.ToggleAppLockUsePin(!state.appLockUsePinEnabled)) },
                    shapes = ShelfShapes.segmented(1, 3),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = containerColor
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Pin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (state.appLockUsePinEnabled) "Device screen lock fallback enabled" else "Biometrics only",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = state.appLockUsePinEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAppLockUsePin(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    content = {
                        Text(
                            text = "Use PIN",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )

                SegmentedListItem(
                    onClick = { viewModel.onEvent(SettingEvents.ShowAppLockTimeoutSheet) },
                    shapes = ShelfShapes.segmented(2, 3),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = containerColor
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    supportingContent = {
                        Text(
                            text = when (state.appLockTimeout) {
                                0L -> "Immediately"
                                60_000L -> "1 minute"
                                300_000L -> "5 minutes"
                                900_000L -> "15 minutes"
                                else -> "Immediately"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    content = {
                        Text(
                            text = "Lock After",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}
