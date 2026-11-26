package `in`.iambhvsh.shelf.screens.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import `in`.iambhvsh.shelf.components.home.BookmarkGrid
import `in`.iambhvsh.shelf.components.home.BookmarkList
import `in`.iambhvsh.shelf.components.home.EmptyBookmarkState
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import `in`.iambhvsh.shelf.components.home.TagFilterRow
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    viewMode: ViewMode,
    gridState: LazyStaggeredGridState,
    listState: LazyListState,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val hasTags = state.tags.isNotEmpty() && !state.isSelectionMode

    val canScrollContent = if (viewMode == ViewMode.GRID) {
        gridState.canScrollForward || gridState.canScrollBackward
    } else {
        listState.canScrollForward || listState.canScrollBackward
    }

    LaunchedEffect(canScrollContent, state.bookmarkData.size, state.activeTagFilters) {
        if (!canScrollContent) {
            scrollBehavior?.state?.heightOffset = 0f
            scrollBehavior?.state?.contentOffset = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (state.bookmarkData.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyBookmarkState()
            }
        } else if (viewMode == ViewMode.GRID) {
            BookmarkGrid(
                items = state.bookmarkData,
                gridState = gridState,
                selectedIds = state.selectedIds,
                isSelectionMode = state.isSelectionMode,
                tapAction = tapAction,
                context = context,
                clipboard = clipboard,
                viewModel = viewModel,
                hasTags = hasTags
            )
        } else {
            BookmarkList(
                items = state.bookmarkData,
                listState = listState,
                selectedIds = state.selectedIds,
                isSelectionMode = state.isSelectionMode,
                tapAction = tapAction,
                context = context,
                clipboard = clipboard,
                viewModel = viewModel,
                hasTags = hasTags
            )
        }

        if (hasTags) {
            val scrollOffset = scrollBehavior?.state?.heightOffset?.roundToInt() ?: 0
            val scrollAlpha = scrollBehavior?.state?.let {
                (1f - it.collapsedFraction).coerceIn(0f, 1f)
            } ?: 1f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarTop + 64.dp)
                    .offset { IntOffset(0, scrollOffset) }
                    .graphicsLayer {
                        alpha = scrollAlpha
                        val scale = 1f - ((1f - scrollAlpha) * 0.08f)
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TagFilterRow(
                    tags = state.tags,
                    activeTagIds = state.activeTagFilters,
                    onToggleTag = { viewModel.homeEvents(HomeEvents.ToggleTagFilter(it)) },
                    enabled = scrollAlpha > 0.05f
                )
            }
        }

        LoadingProgress(state.isLoading)
    }
}
