package `in`.iambhvsh.shelf.screens.collection

import `in`.iambhvsh.shelf.features.collection.*
import `in`.iambhvsh.shelf.components.collection.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import `in`.iambhvsh.shelf.components.collection.CollectionCard
import `in`.iambhvsh.shelf.components.collection.CollectionInputSheet
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionScreen(
    onCollectionClick: (Long) -> Unit,
    searchQuery: String = "",
    isSearching: Boolean = false,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    val filteredCollections = remember(state.collections, searchQuery, isSearching) {
        if (!isSearching || searchQuery.isBlank()) {
            state.collections
        } else {
            state.collections.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CollectionEvents.ClearToast)
        }
    }

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearSelection)
    }

    val itemsToDisplay = filteredCollections
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 100.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (itemsToDisplay.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isSearching) {
                    `in`.iambhvsh.shelf.components.home.EmptyBookmarkState(
                        title = "No collections found",
                        description = "No collections matching \"$searchQuery\""
                    )
                } else {
                    `in`.iambhvsh.shelf.components.home.EmptyBookmarkState(
                        title = "No collections yet",
                        description = "Tap + on the toolbar to create your first collection"
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = topPadding, bottom = bottomPadding),
                verticalItemSpacing = 6.dp,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = itemsToDisplay,
                    key = { it.id },
                    contentType = { "collection_card" }
                ) { collection ->
                    CollectionCard(
                        collection = collection,
                        onClick = {
                            viewModel.onEvent(CollectionEvents.SelectCollection(collection))
                            onCollectionClick(collection.id)
                        },
                        onLongClick = {
                            if (!isSearching) {
                                viewModel.onEvent(CollectionEvents.ToggleSelection(collection.id))
                            }
                        },
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
        `in`.iambhvsh.shelf.components.home.RenameSheet(
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
