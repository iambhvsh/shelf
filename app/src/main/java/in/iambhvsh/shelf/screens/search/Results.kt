package `in`.iambhvsh.shelf.screens.search

import `in`.iambhvsh.shelf.features.search.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.components.home.BookmarkCard
import `in`.iambhvsh.shelf.components.home.BookmarkListItem
import `in`.iambhvsh.shelf.components.home.EmptyBookmarkState
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import `in`.iambhvsh.shelf.features.setting.ViewMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchResults(
    results: List<Bookmark>,
    query: String,
    viewMode: ViewMode,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    isLoading: Boolean,
    onBodyClick: ((Bookmark) -> Unit)? = null,
    onPhotoClick: ((String) -> Unit)? = null,
    onLongClick: ((Long) -> Unit)? = null,
    onNoteClick: ((Bookmark) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()

    LaunchedEffect(query) {
        if (results.isNotEmpty()) {
            if (viewMode == ViewMode.GRID) gridState.scrollToItem(0)
            else listState.scrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (query.isNotEmpty() && results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyBookmarkState(
                    title = "No results found",
                    description = "We couldn't find anything matching \"$query\""
                )
            }
        } else if (query.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyBookmarkState(
                    title = "Search your shelf",
                    description = "Type keywords, titles, or domains to search"
                )
            }
        } else {
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val topPadding = statusBarTop + 68.dp

            if (viewMode == ViewMode.GRID) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    columns = StaggeredGridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = 108.dp),
                    verticalItemSpacing = 6.dp,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = results,
                        key = { it.id },
                        contentType = { "search_result" }
                    ) { item ->
                        BookmarkCard(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            isPinned = item.isPinned,
                            photoClickUrl = { onPhotoClick?.invoke(it) },
                            bodyClick = { onBodyClick?.invoke(item) },
                            onLongClick = { onLongClick?.invoke(item.id) },
                            onNoteClick = { onNoteClick?.invoke(item) },
                            isSelected = item.id in selectedIds,
                            isSelectionMode = isSelectionMode,
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
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = 108.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = results,
                        key = { it.id },
                        contentType = { "search_result" }
                    ) { item ->
                        BookmarkListItem(
                            modifier = Modifier.animateItem(),
                            imageUrl = item.imageUrl,
                            title = item.title,
                            description = item.description,
                            photoClickUrl = { onPhotoClick?.invoke(it) },
                            bodyClick = { onBodyClick?.invoke(item) },
                            onLongClick = { onLongClick?.invoke(item.id) },
                            onNoteClick = { onNoteClick?.invoke(item) },
                            isSelected = item.id in selectedIds,
                            isSelectionMode = isSelectionMode,
                            url = item.url,
                            note = item.note,
                            reminderTime = item.reminderTime
                        )
                    }
                }
            }
        }
        LoadingProgress(isLoading)
    }
}
