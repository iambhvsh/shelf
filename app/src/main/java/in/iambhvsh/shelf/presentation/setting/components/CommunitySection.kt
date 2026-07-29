package `in`.iambhvsh.shelf.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.openChromeTab

@Composable
fun CommunitySection(context: Context) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Community")
    SettingItem(
        icon = Icons.Outlined.Star,
        title = "Star on GitHub",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = Icons.Outlined.BugReport,
        title = "Report Issue",
        onClick = { openChromeTab("https://github.com/iambhvsh/shelf/issues", context) }
    )
}
