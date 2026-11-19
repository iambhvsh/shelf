package `in`.iambhvsh.shelf.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import `in`.iambhvsh.shelf.collection.components.CollectionCard
import `in`.iambhvsh.shelf.collection.components.CollectionInputSheet
import `in`.iambhvsh.shelf.home.components.LoadingProgress
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionScreen(
    onCollectionClick: (Long) -> Unit,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CollectionEvents.ClearToast)
        }
    }

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearSelection)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (state.collections.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                `in`.iambhvsh.shelf.home.components.EmptyBookmarkState(
                    title = "No collections yet",
                    description = "Tap + on the toolbar to create your first collection"
                )
            }
        } else {
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val topPadding = statusBarTop + 68.dp

            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = 108.dp),
                verticalItemSpacing = 6.dp,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = state.collections,
                    key = { it.id }
                ) { collection ->
                    CollectionCard(
                        collection = collection,
                        onClick = {
                            viewModel.onEvent(CollectionEvents.SelectCollection(collection))
                            onCollectionClick(collection.id)
                        },
                        onLongClick = { viewModel.onEvent(CollectionEvents.ToggleSelection(collection.id)) },
                        isSelected = collection.id in state.selectedIds,
                        isSelectionMode = state.isSelectionMode,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
        LoadingProgress(state.isLoading)
    }

    if (state.showRenameCollectionDialog) {
        `in`.iambhvsh.shelf.home.components.RenameSheet(
            initialText = state.renameCollectionDialogText ?: "",
            title = "Rename Collection",
            onDismissRequest = { viewModel.onEvent(CollectionEvents.HideRenameCollectionDialog) },
            onSaveClick = { newName ->
                val id = state.selectedIds.firstOrNull()
                if (id != null) {
                    viewModel.onEvent(CollectionEvents.UpdateCollectionName(id, newName))
                }
            }
        )
    }
}
