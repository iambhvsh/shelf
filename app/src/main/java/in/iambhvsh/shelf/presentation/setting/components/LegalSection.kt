package `in`.iambhvsh.shelf.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.openChromeTab

@Composable
fun LegalSection(context: Context) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Legal")
    SettingItem(
        icon = R.drawable.privacy_policy_icon,
        title = "Privacy Policy",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.terms_icons,
        title = "Terms & Conditions",
        onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) }
    )
}
