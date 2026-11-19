@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.components.common.expressiveBounce

@Composable
fun RightActionSlot(
    isSelectionMode: Boolean,
    currentTab: Int,
    isAllSelected: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onAddToCollection: (() -> Unit)?,
    onRenameSelected: (() -> Unit)?,
    onDeleteSelected: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = if (isSelectionMode) "selection" else "normal",
        modifier = modifier,
        transitionSpec = {
            (fadeIn(animationSpec = tween(180)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(180)) +
                    slideInHorizontally(initialOffsetX = { it / 2 }, animationSpec = tween(180)))
                .togetherWith(
                    fadeOut(animationSpec = tween(150)) +
                            scaleOut(targetScale = 0.92f, animationSpec = tween(150)) +
                            slideOutHorizontally(targetOffsetX = { it / 2 }, animationSpec = tween(150))
                )
        },
        label = "RightActionsContent"
    ) { state ->
        if (state == "selection") {
            Row(
                modifier = Modifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isAllSelected) {
                    IconButton(
                        onClick = onDeselectAll,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .expressiveBounce(scaleDown = 0.92f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.deselct_all),
                            contentDescription = "Deselect all",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSelectAll,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .expressiveBounce(scaleDown = 0.92f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.selectall_icon),
                            contentDescription = "Select all",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (onAddToCollection != null) {
                    IconButton(
                        onClick = onAddToCollection,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .expressiveBounce(scaleDown = 0.92f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.bookmark_add),
                            contentDescription = "Add to collection",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (onRenameSelected != null) {
                    IconButton(
                        onClick = onRenameSelected,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .expressiveBounce(scaleDown = 0.92f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Rename",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (onDeleteSelected != null) {
                    IconButton(
                        onClick = onDeleteSelected,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .expressiveBounce(scaleDown = 0.92f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete_icon),
                            contentDescription = "Delete selected",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        } else {
            val showRightActions = currentTab == 0 || currentTab == 1
            AnimatedVisibility(
                visible = showRightActions,
                enter = fadeIn(animationSpec = tween(180)) +
                        scaleIn(initialScale = 0.88f, animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.88f, animationSpec = tween(150))
            ) {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .expressiveBounce(scaleDown = 0.92f)
                            .clickable(onClick = onSearchClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search_icon),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .expressiveBounce(scaleDown = 0.92f)
                            .clickable(onClick = onSortClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.filter_icon),
                            contentDescription = "Sort & Filter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
