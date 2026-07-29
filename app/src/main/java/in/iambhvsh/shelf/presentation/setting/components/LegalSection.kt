package `in`.iambhvsh.shelf.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.openChromeTab

@Composable
fun LegalSection(context: Context) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Legal")
    SettingItem(
        icon = Icons.Outlined.Shield,
        title = "Privacy Policy",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = Icons.Outlined.Description,
        title = "Terms & Conditions",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) }
    )
}
