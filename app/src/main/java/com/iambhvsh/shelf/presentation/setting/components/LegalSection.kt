package com.iambhvsh.shelf.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iambhvsh.shelf.R
import com.iambhvsh.shelf.openChromeTab

@Composable
fun LegalSection(context: Context) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Legal")
    SettingItem(
        icon = R.drawable.privacy_policy_icon,
        title = "Privacy Policy",
        onClick = { openChromeTab("https://example.com/TODO-your-privacy-policy", context) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.terms_icons,
        title = "Terms & Conditions",
        onClick = { openChromeTab("https://example.com/TODO-your-terms", context) }
    )
}
