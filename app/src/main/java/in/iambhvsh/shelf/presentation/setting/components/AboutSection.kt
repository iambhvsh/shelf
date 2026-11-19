package `in`.iambhvsh.shelf.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Update
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

@Composable
fun AboutSection(
    context: Context,
    versionName: String,
    onChangelogClick: () -> Unit,
    onAboutClick: () -> Unit,
    onCheckForUpdatesClick: () -> Unit
) {
    Spacer(Modifier.height(ShelfShapes.Gap * 4))
    SectionHeader("Support")
    SettingItem(
        icon = Icons.Outlined.BugReport,
        title = "Report Issue",
        index = 0,
        count = 2,
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf/issues", context) }
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.Star,
        title = "Star on GitHub",
        index = 1,
        count = 2,
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) }
    )

    Spacer(Modifier.height(ShelfShapes.Gap * 4))
    SectionHeader("Legal")
    SettingItem(
        icon = Icons.Outlined.Policy,
        title = "Privacy Policy",
        index = 0,
        count = 2,
        onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) }
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.Gavel,
        title = "Terms & Conditions",
        index = 1,
        count = 2,
        onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) }
    )

    Spacer(Modifier.height(ShelfShapes.Gap * 4))
    SectionHeader("About")
    SettingItem(
        icon = Icons.Outlined.Info,
        title = "About Shelf",
        index = 0,
        count = 3,
        subtitle = "The links you love. Beautifully organized.",
        onClick = onAboutClick
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.AutoAwesome,
        title = "Changelog",
        index = 1,
        count = 3,
        subtitle = "See what's new in the latest versions",
        onClick = onChangelogClick
    )
    Spacer(Modifier.height(ShelfShapes.Gap))
    SettingItem(
        icon = Icons.Outlined.Update,
        title = "Check for updates",
        index = 2,
        count = 3,
        subtitle = versionName,
        onClick = onCheckForUpdatesClick
    )
    Spacer(Modifier.height(24.dp))
}
