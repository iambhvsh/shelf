package `in`.iambhvsh.shelf.screens.collection

import `in`.iambhvsh.shelf.features.collection.*
import `in`.iambhvsh.shelf.components.collection.*

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.components.home.BookmarkCard
import `in`.iambhvsh.shelf.components.home.BookmarkListItem
import `in`.iambhvsh.shelf.components.home.EmptyBookmarkState
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import `in`.iambhvsh.shelf.screens.search.SearchResults
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    viewModel: CollectionViewModel,
    searchResults: List<Bookmark>? = null,
    searchQuery: String = ""
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val itemCount = state.collectionBookmarks.size
    var prevCount by rememberSaveable { mutableIntStateOf(itemCount) }
    var bookmarkToDelete by remember { mutableStateOf<Bookmark?>(null) }

    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(CollectionEvents.ClearSelectedCollection) }
    }

    LaunchedEffect(itemCount) {
        if (itemCount > prevCount && itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.animateScrollToItem(0)
            else listState.animateScrollToItem(0)
        }
        prevCount = itemCount
    }

    LaunchedEffect(state.sortOrder) {
        if (itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.scrollToItem(0)
            else listState.scrollToItem(0)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CollectionEvents.ClearToast)
        }
    }

    BackHandler(enabled = state.isDetailSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearDetailSelection)
    }

    LaunchedEffect(collectionId) {
        if (state.selectedCollection?.id != collectionId) {
            val collection = state.collections.find { it.id == collectionId }
            if (collection != null) {
                viewModel.onEvent(CollectionEvents.SelectCollection(collection))
            }
        }
    }

    if (searchResults != null) {
        SearchResults(
            results = searchResults,
            query = searchQuery,
            viewMode = viewMode,
            selectedIds = state.detailSelectedIds,
            isSelectionMode = state.isDetailSelectionMode,
            isLoading = state.isDetailLoading,
            onBodyClick = { item ->
                when (tapAction) {
                    TapAction.OPEN_BROWSER -> item.url.let { openChromeTab(it, context) }
                    TapAction.COPY_LINK -> item.url.let { clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText("", it)) }
                    TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                }
            },
            onPhotoClick = { Log.d("CollectionDetail", "Photo click: $it") },
            onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(it)) }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.collectionBookmarks.isEmpty() && !state.isDetailLoading) {
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
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        columns = StaggeredGridCells.Adaptive(180.dp),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding),
                        verticalItemSpacing = 6.dp,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(items = state.collectionBookmarks, key = { it.id }) { item ->
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
                                        TapAction.COPY_LINK -> item.url.let { clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText("", it)) }
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
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = state.collectionBookmarks, key = { it.id }) { item ->
                            BookmarkListItem(
                                modifier = Modifier.animateItem(),
                                imageUrl = item.imageUrl,
                                title = item.title,
                                description = item.description,
                                photoClickUrl = { Log.d("CollectionDetail", "Photo click: $it") },
                                bodyClick = {
                                    when (tapAction) {
                                        TapAction.OPEN_BROWSER -> item.url.let { openChromeTab(it, context) }
                                        TapAction.COPY_LINK -> item.url.let { clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText("", it)) }
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

    CollectionDetailSheets(
        state = state,
        viewModel = viewModel,
        context = context,
        clipboard = clipboardManager,
        bookmarkToDelete = bookmarkToDelete,
        onSetBookmarkToDelete = { bookmarkToDelete = it }
    )
}
