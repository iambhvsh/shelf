package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutSection(
    versionName: String,
    onAboutClick: () -> Unit,
    onCheckForUpdatesClick: () -> Unit
) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("About")
    SettingItem(
        icon = Icons.Outlined.Info,
        title = "About Shelf",
        subtitle = "The links you love. Beautifully organized.",
        onClick = onAboutClick
    )
    SettingItem(
        icon = Icons.Outlined.Verified,
        title = "App version",
        subtitle = versionName,
        onClick = { }
    )
    SettingItem(
        icon = Icons.Outlined.SystemUpdate,
        title = "Check for updates",
        subtitle = "Install latest version from GitHub",
        onClick = onCheckForUpdatesClick
    )
}
