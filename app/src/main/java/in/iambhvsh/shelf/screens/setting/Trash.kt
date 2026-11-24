@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import `in`.iambhvsh.shelf.components.common.ExpressiveFilledTonalButton
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.components.home.DeleteConfirmationDialog
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.theme.HeroFont
import `in`.iambhvsh.shelf.theme.ShelfShapes
import kotlinx.coroutines.launch

private val SwipeRestoreGreen = Color(0xFF2E7D32)
private val OnSwipeRestoreGreen = Color.White

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsTrashScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingSwipeDeleteBookmark by remember { mutableStateOf<Bookmark?>(null) }
    var pendingDismissState by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 84.dp

    BackHandler(enabled = state.selectedTrashIds.isNotEmpty()) {
        viewModel.onEvent(SettingEvents.ClearTrashSelection)
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingEvents.LoadTrash)
    }

    state.trashToastMessage?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SettingEvents.ClearTrashToast)
        }
    }

    if (pendingSwipeDeleteBookmark != null) {
        DeleteConfirmationDialog(
            title = "Delete permanently",
            text = "Are you sure you want to permanently delete this bookmark? This cannot be undone.",
            onConfirm = {
                pendingSwipeDeleteBookmark?.let {
                    viewModel.onEvent(SettingEvents.DeleteTrashItem(it.id))
                }
                pendingSwipeDeleteBookmark = null
                pendingDismissState = null
            },
            onDismiss = {
                val stateToReset = pendingDismissState
                coroutineScope.launch {
                    stateToReset?.reset()
                }
                pendingSwipeDeleteBookmark = null
                pendingDismissState = null
            }
        )
    }

    if (state.showDeletePermanentlyDialog) {
        DeleteConfirmationDialog(
            title = "Delete permanently",
            text = "Are you sure you want to permanently delete the selected bookmark(s)? This cannot be undone.",
            onConfirm = { viewModel.onEvent(SettingEvents.ConfirmDeletePermanently) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideDeletePermanentlyDialog) }
        )
    }

    if (state.showEmptyTrashDialog) {
        DeleteConfirmationDialog(
            title = "Empty trash",
            text = "Are you sure you want to permanently delete all bookmarks in trash? This cannot be undone.",
            onConfirm = { viewModel.onEvent(SettingEvents.ConfirmEmptyTrash) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideEmptyTrashDialog) }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.trashedBookmarks.isEmpty() && !state.isTrashLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape())
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Trash is empty",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = HeroFont),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Deleted bookmarks will appear here before being permanently removed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val isSelectionMode = state.selectedTrashIds.isNotEmpty()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = topPadding + 8.dp,
                    bottom = bottomPadding
                )
            ) {
                itemsIndexed(
                    items = state.trashedBookmarks,
                    key = { _, item -> item.id }
                ) { _, item ->
                    val isSelected = item.id in state.selectedTrashIds
                    val cardShape = if (isSelected) MaterialTheme.shapes.extraLargeIncreased else MaterialTheme.shapes.largeIncreased

                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        when (dismissState.currentValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                pendingSwipeDeleteBookmark = item
                                pendingDismissState = dismissState
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.onEvent(SettingEvents.RestoreTrashItem(item.id))
                            }
                            SwipeToDismissBoxValue.Settled -> Unit
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = !isSelectionMode,
                        enableDismissFromEndToStart = !isSelectionMode,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val isDeleting = direction == SwipeToDismissBoxValue.StartToEnd
                            val isRestoring = direction == SwipeToDismissBoxValue.EndToStart

                            val backgroundColor by animateColorAsState(
                                targetValue = when {
                                    isDeleting -> MaterialTheme.colorScheme.errorContainer
                                    isRestoring -> SwipeRestoreGreen
                                    else -> Color.Transparent
                                },
                                label = "SwipeBackgroundColor"
                            )

                            val iconTint by animateColorAsState(
                                targetValue = when {
                                    isDeleting -> MaterialTheme.colorScheme.onErrorContainer
                                    isRestoring -> OnSwipeRestoreGreen
                                    else -> Color.Transparent
                                },
                                label = "SwipeIconColor"
                            )

                            val alignment = when {
                                isDeleting -> Alignment.CenterStart
                                isRestoring -> Alignment.CenterEnd
                                else -> Alignment.Center
                            }

                            val icon = when {
                                isDeleting -> Icons.Outlined.DeleteOutline
                                isRestoring -> Icons.Outlined.Restore
                                else -> null
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(cardShape)
                                    .background(backgroundColor)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = alignment
                            ) {
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = if (isDeleting) "Delete" else "Restore",
                                        tint = iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .expressiveBounce(scaleDown = 0.98f)
                                .clip(cardShape)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
                                    else Modifier
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.onEvent(SettingEvents.ToggleTrashSelection(item.id))
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.onEvent(SettingEvents.ToggleTrashSelection(item.id))
                                    }
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AnimatedVisibility(
                                    visible = isSelectionMode,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Row {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleTrashSelection(item.id)) },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val title = item.title?.takeIf { it.isNotBlank() } ?: item.url
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    val desc = item.description?.takeIf { it.isNotBlank() } ?: item.url
                                    if (desc != title) {
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarBottom + 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLargeIncreased,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpressiveFilledTonalButton(
                            onClick = { viewModel.onEvent(SettingEvents.RestoreSelectedTrash) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore (${state.selectedTrashIds.size})")
                        }

                        ExpressiveButton(
                            onClick = { viewModel.onEvent(SettingEvents.ShowDeletePermanentlyDialog) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete (${state.selectedTrashIds.size})")
                        }
                    }
                }
            }
        }
    }
}
