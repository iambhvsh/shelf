package `in`.iambhvsh.shelf.screens.setting

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.aboutExternalLinks(context: Context) {
    item(key = "github_star") {
        SegmentedListItem(
            onClick = { openChromeTab("https://github.com/iambhvsh/shelf", context) },
            shapes = ShelfShapes.segmented(0, 2),
            modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
            modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.aboutLegalLinks(context: Context) {
    item(key = "privacy_policy") {
        SegmentedListItem(
            onClick = { openChromeTab("https://shelf.iambhvsh.in/privacy", context) },
            shapes = ShelfShapes.segmented(0, 2),
            modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
            modifier = Modifier.fillMaxWidth().expressiveBounce(scaleDown = 0.98f),
            colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
