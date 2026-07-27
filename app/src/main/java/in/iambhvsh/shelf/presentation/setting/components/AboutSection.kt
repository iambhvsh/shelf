package in.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import in.iambhvsh.shelf.R

@Composable
fun AboutSection(versionName: String) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("About")
    SettingItem(
        icon = R.drawable.about_icon,
        title = "App version",
        subtitle = versionName,
        onClick = { }
    )
}
