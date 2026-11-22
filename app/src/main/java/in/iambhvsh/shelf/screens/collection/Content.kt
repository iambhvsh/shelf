package `in`.iambhvsh.shelf.screens.collection

import android.content.ClipData
import android.content.Context
import android.util.Log
import `in`.iambhvsh.shelf.components.common.copyToClipboard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.home.BookmarkCard
import `in`.iambhvsh.shelf.components.home.BookmarkListItem
import `in`.iambhvsh.shelf.components.home.EmptyBookmarkState
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import `in`.iambhvsh.shelf.features.collection.CollectionEvents
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.openChromeTab

@Composable
fun CollectionDetailContent(
    state: CollectionState,
    collectionId: Long,
    viewMode: ViewMode,
    gridState: LazyStaggeredGridState,
    listState: LazyListState,
    tapAction: TapAction,
    context: Context,
    clipboardManager: Clipboard,
    viewModel: CollectionViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.collectionBookmarks.isEmpty() && !state.isDetailLoading && state.selectedCollection?.id == collectionId) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyBookmarkState(
                    title = "Collection is empty",
                    description = "Tap + on the toolbar to add bookmarks to this collection"
                )
            }
        } else {
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val topPadding = statusBarTop + 68.dp
            val bottomPadding = navBarBottom + 24.dp

            if (viewMode == ViewMode.GRID) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding),
                    verticalItemSpacing = 6.dp,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = state.collectionBookmarks,
                        key = { it.id },
                        contentType = { "collection_bookmark" }
                    ) { item ->
                        BookmarkCard(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            isPinned = item.isPinned,
                            photoClickUrl = { Log.d("CollectionDetail", "Photo click: $it") },
                            bodyClick = {
                                when (tapAction) {
                                    TapAction.OPEN_BROWSER -> item.url.let { openChromeTab(it, context) }
                                    TapAction.COPY_LINK -> context.copyToClipboard(item.url)
                                    TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                                }
                            },
                            onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(item.id)) },
                            isSelected = item.id in state.detailSelectedIds,
                            isSelectionMode = state.isDetailSelectionMode,
                            url = item.url,
                            note = item.note,
                            reminderTime = item.reminderTime
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = state.collectionBookmarks,
                        key = { it.id },
                        contentType = { "collection_bookmark" }
                    ) { item ->
                        BookmarkListItem(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            photoClickUrl = { Log.d("CollectionDetail", "Photo click: $it") },
                            bodyClick = {
                                when (tapAction) {
                                    TapAction.OPEN_BROWSER -> item.url.let { openChromeTab(it, context) }
                                    TapAction.COPY_LINK -> context.copyToClipboard(item.url)
                                    TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                                }
                            },
                            onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(item.id)) },
                            isSelected = item.id in state.detailSelectedIds,
                            isSelectionMode = state.isDetailSelectionMode,
                            url = item.url,
                            note = item.note,
                            reminderTime = item.reminderTime
                        )
                    }
                }
            }
        }
        LoadingProgress(state.isDetailLoading)
    }
}
