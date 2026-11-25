@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.common.ExpressiveTextButton
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    state: SettingState,
    versionName: String,
    onNavigate: (AppRoute) -> Unit,
    onResetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 100.dp

    val generalGroup = listOf(
        SettingsCategory(
            route = AppRoute.SettingsAppearance,
            icon = Icons.Outlined.Palette,
            title = "Appearance",
            subtitle = "Theme, Dynamic color, Accent color"
        ),
        SettingsCategory(
            route = AppRoute.SettingsGeneral,
            icon = Icons.Outlined.Tune,
            title = "General",
            subtitle = "Tap action, View mode, Permissions"
        ),
        SettingsCategory(
            route = AppRoute.SettingsSecurity,
            icon = Icons.Outlined.Security,
            title = "Security",
            subtitle = if (state.appLockEnabled) "App lock enabled, PIN fallback" else "App lock, Biometrics, PIN"
        )
    )

    val dataCategory = SettingsCategory(
        route = AppRoute.SettingsData,
        icon = Icons.Outlined.Storage,
        title = "Data & Storage",
        subtitle = "Recently deleted, Export, Import, Auto backup"
    )

    val aboutCategory = SettingsCategory(
        route = AppRoute.SettingsAbout,
        icon = Icons.Outlined.Info,
        title = "About",
        subtitle = if (versionName.isNotEmpty()) "Shelf v$versionName \u2022 Updates, GitHub, Legal" else "Updates, GitHub, Legal"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding)
    ) {
        items(generalGroup.size, key = { generalGroup[it].title }) { index ->
            val item = generalGroup[index]
            CategoryListItem(
                item = item,
                index = index,
                count = generalGroup.size,
                onClick = { onNavigate(item.route) }
            )
        }

        item(key = "spacer_data") {
            Spacer(Modifier.height(12.dp))
        }

        item(key = "data_category") {
            CategoryListItem(
                item = dataCategory,
                index = 0,
                count = 1,
                onClick = { onNavigate(dataCategory.route) }
            )
        }

        item(key = "spacer_about") {
            Spacer(Modifier.height(12.dp))
        }

        item(key = "about_category") {
            CategoryListItem(
                item = aboutCategory,
                index = 0,
                count = 1,
                onClick = { onNavigate(aboutCategory.route) }
            )
        }

        item(key = "spacer_reset") {
            Spacer(Modifier.height(16.dp))
        }

        item(key = "reset_button") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ExpressiveTextButton(
                    onClick = onResetClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
