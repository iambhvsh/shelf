package `in`.iambhvsh.shelf.home.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.home.HomeEvents
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.setting.TapAction

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkGrid(
    items: List<Bookmark>,
    gridState: LazyStaggeredGridState,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel,
    hasTags: Boolean = false
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topPadding = statusBarTop + 68.dp + if (hasTags) 48.dp else 0.dp

    LazyVerticalStaggeredGrid(
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = 108.dp),
        verticalItemSpacing = 6.dp,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            BookmarkCard(
                imageUrl = item.imageUrl,
                title = item.title,
                description = item.description,
                bodyClick = {
                    handleTap(
                        item = item,
                        tapAction = tapAction,
                        context = context,
                        clipboard = clipboard,
                        viewModel = viewModel
                    )
                },
                photoClickUrl = { url ->
                    viewModel.homeEvents(HomeEvents.PreviewImageClick(url = url))
                },
                onLongClick = {
                    viewModel.homeEvents(HomeEvents.ToggleSelection(item.id))
                },
                isSelected = selectedIds.contains(item.id),
                isSelectionMode = isSelectionMode,
                onNoteClick = {
                    viewModel.homeEvents(HomeEvents.ShowNoteEditor(item.note))
                },
                isPinned = item.isPinned,
                url = item.url,
                note = item.note,
                reminderTime = item.reminderTime
            )
        }
    }
}
