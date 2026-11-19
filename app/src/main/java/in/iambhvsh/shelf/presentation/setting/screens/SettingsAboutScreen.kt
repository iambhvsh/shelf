@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.presentation.setting.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.presentation.setting.SettingEvents
import `in`.iambhvsh.shelf.presentation.setting.SettingState
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.HeroFont
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    versionName: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding)
    ) {
        // App Hero Card
        item(key = "hero_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShelfShapes.card)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.shelf_logo),
                        contentDescription = "Shelf Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialShapes.Cookie12Sided.toShape())
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Shelf",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = HeroFont),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (versionName.isNotEmpty()) {
                            Text(
                                text = "v$versionName",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "The links you love. Beautifully organized.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item(key = "spacer_updates") {
            Spacer(Modifier.height(12.dp))
        }

        // Group 1: Updates & App Info (count = 3)
        item(key = "check_updates") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.CheckForUpdates) },
                shapes = ShelfShapes.segmented(0, 3),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Update,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = if (versionName.isNotEmpty()) "Current version v$versionName" else "Check for new releases",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Check for updates",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "changelog") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ShowChangelogSheet) },
                shapes = ShelfShapes.segmented(1, 3),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "See what's new in recent versions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Changelog",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "about_shelf") {
            SegmentedListItem(
                onClick = { viewModel.onEvent(SettingEvents.ShowAboutSheet) },
                shapes = ShelfShapes.segmented(2, 3),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "Developer, acknowledgements & license",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "About Shelf",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "spacer_community") {
            Spacer(Modifier.height(12.dp))
        }

        // Group 2: Community & Support (count = 2)
        item(key = "github_star") {
            SegmentedListItem(
                onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) },
                shapes = ShelfShapes.segmented(0, 2),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "github.com/iambhvsh/shelf",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Star on GitHub",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "report_issue") {
            SegmentedListItem(
                onClick = { openChromeTab("https://github.com/iambhvsh/shelf/issues", context) },
                shapes = ShelfShapes.segmented(1, 2),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "Found a bug or have a suggestion?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Report Issue",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "spacer_legal") {
            Spacer(Modifier.height(12.dp))
        }

        // Group 3: Legal (count = 2)
        item(key = "privacy_policy") {
            SegmentedListItem(
                onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) },
                shapes = ShelfShapes.segmented(0, 2),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "shelf.iambhvsh.in/privacy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        item(key = "terms") {
            SegmentedListItem(
                onClick = { openChromeTab("https://shelf.iambhvsh.in/terms", context) },
                shapes = ShelfShapes.segmented(1, 2),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingContent = {
                    Text(
                        text = "shelf.iambhvsh.in/terms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                content = {
                    Text(
                        text = "Terms & Conditions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}
