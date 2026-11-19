package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes
import `in`.iambhvsh.shelf.ui.theme.ThemeMode

@Composable
fun ThemeSection(state: SettingState, viewModel: SettingViewModel) {
    SectionHeader("Theme")
    val hasAccent = !state.isDynamicColorSupported || !state.dynamicColor
    val count = if (state.isDynamicColorSupported) {
        if (hasAccent) 3 else 2
    } else 2

    SettingItem(
        icon = Icons.Outlined.DarkMode,
        title = "Theme",
        index = 0,
        count = count,
        subtitle = when (state.themeMode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.OLED -> "Oled"
            ThemeMode.SYSTEM -> "System"
        },
        onClick = { viewModel.onEvent(SettingEvents.ShowThemeSheet) }
    )
    if (state.isDynamicColorSupported) {
        Spacer(Modifier.height(ShelfShapes.Gap))
        SettingItem(
            icon = Icons.Outlined.ColorLens,
            title = "Dynamic color",
            index = 1,
            count = count,
            subtitle = if (state.dynamicColor) "On" else "Off",
            trailing = {
                Switch(
                    checked = state.dynamicColor,
                    onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            onClick = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(!state.dynamicColor)) }
        )
    }
    if (hasAccent) {
        Spacer(Modifier.height(ShelfShapes.Gap))
        SettingItem(
            icon = Icons.Outlined.Palette,
            title = "Accent color",
            index = if (state.isDynamicColorSupported) 2 else 1,
            count = count,
            subtitle = state.accentColor.label,
            onClick = { viewModel.onEvent(SettingEvents.ShowAccentColorSheet) }
        )
    }
}
