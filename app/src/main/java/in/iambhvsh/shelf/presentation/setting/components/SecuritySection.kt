package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

@Composable
fun SecuritySection(
    state: SettingState,
    viewModel: SettingViewModel
) {
    Spacer(Modifier.height(ShelfShapes.Gap * 4))
    SectionHeader("Security")
    val count = if (state.appLockEnabled) 3 else 1

    SettingItem(
        icon = Icons.Outlined.Lock,
        title = "App Lock",
        index = 0,
        count = count,
        subtitle = if (state.appLockEnabled) "Enabled (Biometrics)" else "Off",
        trailing = {
            Switch(
                checked = state.appLockEnabled,
                onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAppLock(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        onClick = { viewModel.onEvent(SettingEvents.ToggleAppLock(!state.appLockEnabled)) }
    )

    if (state.appLockEnabled) {
        Spacer(Modifier.height(ShelfShapes.Gap))
        SettingItem(
            icon = Icons.Outlined.Pin,
            title = "Use PIN",
            index = 1,
            count = 3,
            subtitle = if (state.appLockUsePinEnabled) "PIN fallback enabled" else "Fingerprint only",
            trailing = {
                Switch(
                    checked = state.appLockUsePinEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleAppLockUsePin(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            onClick = { viewModel.onEvent(SettingEvents.ToggleAppLockUsePin(!state.appLockUsePinEnabled)) }
        )

        Spacer(Modifier.height(ShelfShapes.Gap))
        SettingItem(
            icon = Icons.Outlined.Timer,
            title = "Lock After",
            index = 2,
            count = 3,
            subtitle = when (state.appLockTimeout) {
                0L -> "Immediately"
                60_000L -> "1 minute"
                300_000L -> "5 minutes"
                900_000L -> "15 minutes"
                else -> "Immediately"
            },
            onClick = { viewModel.onEvent(SettingEvents.ShowAppLockTimeoutSheet) }
        )
    }
}
