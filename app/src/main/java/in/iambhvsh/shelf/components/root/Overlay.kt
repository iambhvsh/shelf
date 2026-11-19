package `in`.iambhvsh.shelf.components.root

import android.content.Context
import androidx.compose.runtime.Composable
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.features.collection.CollectionEvents
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.components.collection.CollectionInputSheet
import `in`.iambhvsh.shelf.components.collection.CollectionPickerSheet
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.components.home.DeleteConfirmationDialog
import `in`.iambhvsh.shelf.components.home.HomeInputSheet
import `in`.iambhvsh.shelf.features.setting.BrowserImportState
import `in`.iambhvsh.shelf.features.setting.ExportState
import `in`.iambhvsh.shelf.features.setting.ImportState
import `in`.iambhvsh.shelf.features.setting.SettingDialogsAndSheets
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.components.setting.RadioOptionSheet
import `in`.iambhvsh.shelf.components.common.LoadingProgress

@Composable
fun RootOverlays(
    state: HomeState,
    viewModel: HomeViewModel,
    collectionState: CollectionState,
    collectionViewModel: CollectionViewModel,
    settingState: SettingState,
    settingViewModel: SettingViewModel,
    showDeleteConfirmDialog: Boolean,
    onDismissDeleteConfirm: () -> Unit,
    pendingDeleteAction: (() -> Unit)?,
    versionName: String,
    context: Context
) {
    val isSettingBusy =
        settingState.browserImportState is BrowserImportState.Loading ||
                settingState.importState is ImportState.Loading ||
                settingState.exportState is ExportState.Loading ||
                settingState.browserExportState is ExportState.Loading ||
                settingState.isCheckingForUpdates

    LoadingProgress(isLoading = isSettingBusy, blockTouch = true)

    HomeInputSheet(
        showBottomSheet = state.isDialog,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.OnDialogDismissClick) },
        value = state.inputUrl,
        onTextChange = { viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(it)) },
        onSaveClick = { viewModel.homeEvents(HomeEvents.SaveBookmark) }
    )

    CollectionInputSheet(
        showBottomSheet = collectionState.showCreateDialog,
        onDismissRequest = { collectionViewModel.onEvent(CollectionEvents.HideCreateDialog) },
        value = collectionState.inputName,
        onTextChange = { collectionViewModel.onEvent(CollectionEvents.InputNameChanged(it)) },
        onSaveClick = { collectionViewModel.onEvent(CollectionEvents.CreateCollection) }
    )

    if (state.showCollectionPicker) {
        CollectionPickerSheet(
            collections = state.collections.filter { !it.isVirtual },
            onSelectCollection = { viewModel.homeEvents(HomeEvents.AddToCollection(it)) },
            onDismiss = { viewModel.homeEvents(HomeEvents.HideCollectionPicker) }
        )
    }

    if (state.showSortSheet) {
        RadioOptionSheet(
            title = "Sort by",
            options = listOf(
                "Date added (newest first)" to SortOrder.DATE_NEWEST,
                "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                "Title (A-Z)" to SortOrder.TITLE_ASC,
                "Title (Z-A)" to SortOrder.TITLE_DESC
            ),
            current = state.sortOrder,
            onSelect = { viewModel.homeEvents(HomeEvents.SetSortOrder(it)) },
            onDismiss = { viewModel.homeEvents(HomeEvents.HideSortSheet) }
        )
    }

    if (collectionState.showSortSheet) {
        RadioOptionSheet(
            title = "Sort by",
            options = listOf(
                "Date added (newest first)" to SortOrder.DATE_NEWEST,
                "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                "Title (A-Z)" to SortOrder.TITLE_ASC,
                "Title (Z-A)" to SortOrder.TITLE_DESC
            ),
            current = collectionState.sortOrder,
            onSelect = { collectionViewModel.onEvent(CollectionEvents.SetSortOrder(it)) },
            onDismiss = { collectionViewModel.onEvent(CollectionEvents.HideSortSheet) }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            title = "Delete Selected",
            text = "Are you sure you want to permanently delete these items?",
            onConfirm = {
                pendingDeleteAction?.invoke()
                onDismissDeleteConfirm()
            },
            onDismiss = onDismissDeleteConfirm
        )
    }

    SettingDialogsAndSheets(
        state = settingState,
        viewModel = settingViewModel,
        versionName = versionName,
        context = context
    )
}
