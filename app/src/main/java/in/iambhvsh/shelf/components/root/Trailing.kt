@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    isCollectionDetail: Boolean = false,
    onNewBookmarkClick: (() -> Unit)? = null,
    isTrashScreen: Boolean = false,
    onRestoreSelected: (() -> Unit)? = null,
    onEmptyTrash: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = if (isSelectionMode) "selection" else "normal",
        modifier = modifier,
        transitionSpec = {
            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    slideInHorizontally(initialOffsetX = { it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                .togetherWith(
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideOutHorizontally(targetOffsetX = { it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                )
        },
        label = "RightActionsContent"
    ) { state ->
        if (state == "selection") {
            Row(
                modifier = Modifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isAllSelected) {
                    TopBarButton(
                        onClick = onDeselectAll,
                        contentDescription = "Deselect all",
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.deselct_all),
                            contentDescription = "Deselect all",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    TopBarButton(
                        onClick = onSelectAll,
                        contentDescription = "Select all",
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.selectall_icon),
                            contentDescription = "Select all",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (onRestoreSelected != null) {
                    TopBarButton(
                        onClick = onRestoreSelected,
                        contentDescription = "Restore",
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restore,
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                if (onAddToCollection != null) {
                    TopBarButton(
                        onClick = onAddToCollection,
                        contentDescription = "Add to collection",
                        contentColor = MaterialTheme.colorScheme.primary
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
                    TopBarButton(
                        onClick = onRenameSelected,
                        contentDescription = "Rename",
                        contentColor = MaterialTheme.colorScheme.primary
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
                    TopBarButton(
                        onClick = onDeleteSelected,
                        contentDescription = "Delete selected",
                        contentColor = MaterialTheme.colorScheme.error
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
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(initialScale = 0.88f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleOut(targetScale = 0.88f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                var showMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TopBarButton(
                        onClick = { showMenu = true },
                        contentDescription = "More options",
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    MoreActionDropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        onSearchClick = onSearchClick,
                        onSortClick = onSortClick,
                        onAddClick = if (isCollectionDetail) onNewBookmarkClick else null,
                        addLabel = "Add Bookmark"
                    )
                }
            }
        }
    }
}
