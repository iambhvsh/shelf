package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel

@Composable
fun SecuritySection(
    state: SettingState,
    viewModel: SettingViewModel
) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Security")
    SettingItem(
        icon = Icons.Outlined.Lock,
        title = "App Lock",
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
    
    androidx.compose.animation.AnimatedVisibility(visible = state.appLockEnabled) {
        SettingItem(
            icon = androidx.compose.material.icons.Icons.Outlined.Lock,
            title = "Use PIN",
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
    }
}
