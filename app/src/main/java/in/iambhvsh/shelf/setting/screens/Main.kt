@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.setting.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.setting.SettingState
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

private data class SettingsCategory(
    val route: AppRoute,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

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
    val topPadding = statusBarTop + 68.dp

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
            subtitle = "Default tap action, View mode"
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
        subtitle = "Export, Import, Auto backup"
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = 108.dp)
    ) {
        // Group 1: Appearance, General, Security
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

        // Group 2: Data & Storage
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

        // Group 3: About
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
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onResetClick
                ) {
                    Text(
                        text = "Reset",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategoryListItem(
    item: SettingsCategory,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ShelfShapes.segmented(index, count),
        modifier = modifier
            .fillMaxWidth()
            .expressiveBounce(scaleDown = 0.98f),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        leadingContent = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        supportingContent = {
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        },
        content = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
