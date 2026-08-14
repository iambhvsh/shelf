package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import android.content.Context
import `in`.iambhvsh.shelf.openChromeTab

@Composable
fun AboutSection(
    context: Context,
    versionName: String,
    onChangelogClick: () -> Unit,
    onAboutClick: () -> Unit,
    onCheckForUpdatesClick: () -> Unit
) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("About")
    SettingItem(
        icon = Icons.Outlined.History,
        title = "Changelog",
        subtitle = "See what's new in the latest versions",
        onClick = onChangelogClick
    )
    SettingItem(
        icon = Icons.Outlined.Info,
        title = "About Shelf",
        subtitle = "The links you love. Beautifully organized.",
        onClick = onAboutClick
    )
    SettingItem(
        icon = Icons.Outlined.Star,
        title = "Star on GitHub",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) }
    )
    SettingItem(
        icon = Icons.Outlined.BugReport,
        title = "Report Issue",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf/issues", context) }
    )
    SettingItem(
        icon = Icons.Outlined.Shield,
        title = "Privacy Policy",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) }
    )
    SettingItem(
        icon = Icons.Outlined.Description,
        title = "Terms & Conditions",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) }
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
