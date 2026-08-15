package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Gavel
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
    SectionHeader("Support")
    SettingItem(
        icon = Icons.Outlined.BugReport,
        title = "Report Issue",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf/issues", context) }
    )
    SettingItem(
        icon = Icons.Outlined.Star,
        title = "Star on GitHub",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) }
    )

    Spacer(Modifier.height(12.dp))
    SectionHeader("Legal")
    SettingItem(
        icon = Icons.Outlined.Policy,
        title = "Privacy Policy",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) }
    )
    SettingItem(
        icon = Icons.Outlined.Gavel,
        title = "Terms & Conditions",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) }
    )

    Spacer(Modifier.height(12.dp))
    SectionHeader("About")
    SettingItem(
        icon = Icons.Outlined.Info,
        title = "About Shelf",
        subtitle = "The links you love. Beautifully organized.",
        onClick = onAboutClick
    )
    SettingItem(
        icon = Icons.Outlined.AutoAwesome,
        title = "Changelog",
        subtitle = "See what's new in the latest versions",
        onClick = onChangelogClick
    )
    SettingItem(
        icon = Icons.Outlined.Update,
        title = "Check for updates",
        subtitle = versionName,
        onClick = onCheckForUpdatesClick
    )
    Spacer(Modifier.height(24.dp))
}
