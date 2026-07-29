package `in`.iambhvsh.shelf.presentation.home.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.presentation.home.HomeEvents
import `in`.iambhvsh.shelf.presentation.home.HomeViewModel
import `in`.iambhvsh.shelf.presentation.setting.TapAction

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
    viewModel: HomeViewModel
) {
    LazyVerticalStaggeredGrid(
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 6.dp,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            BookmarkCard(
                modifier = Modifier.animateItem(),
                imageUrl = item.imageUrl,
                title = item.title,
                description = item.description,
                isPinned = item.isPinned,
                url = item.url,
                note = item.note,
                reminderTime = item.reminderTime,
                photoClickUrl = {
                    viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it))
                    Log.d("Photo Dialog", "HomeScreen: $it")
                },
                bodyClick = { handleTap(item, tapAction, context, clipboard, viewModel) },
                onLongClick = { viewModel.homeEvents(HomeEvents.ToggleSelection(item.id)) },
                isSelected = item.id in selectedIds,
                isSelectionMode = isSelectionMode
            )
        }
    }
}
