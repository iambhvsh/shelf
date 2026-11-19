package `in`.iambhvsh.shelf.home.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.home.HomeEvents
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.setting.TapAction

@Composable
fun BookmarkList(
    items: List<Bookmark>,
    listState: LazyListState,
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

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            BookmarkListItem(
                modifier = Modifier.animateItem(),
                imageUrl = item.imageUrl,
                title = item.title,
                description = item.description,
                photoClickUrl = { viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it)) },
                bodyClick = { handleTap(item, tapAction, context, clipboard, viewModel) },
                onLongClick = { viewModel.homeEvents(HomeEvents.ToggleSelection(item.id)) },
                isSelected = item.id in selectedIds,
                isSelectionMode = isSelectionMode,
                url = item.url,
                note = item.note,
                reminderTime = item.reminderTime,
                onNoteClick = { 
                    viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                    viewModel.homeEvents(HomeEvents.ShowNoteEditor(item.note))
                }
            )
        }
    }
}
