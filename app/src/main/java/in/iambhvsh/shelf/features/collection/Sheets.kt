package `in`.iambhvsh.shelf.features.collection

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import `in`.iambhvsh.shelf.components.common.copyToClipboard
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.components.home.BookmarkPreviewSheet
import `in`.iambhvsh.shelf.components.home.DeleteConfirmationDialog
import `in`.iambhvsh.shelf.components.home.EditBookmarkSheet
import `in`.iambhvsh.shelf.components.home.HomeInputSheet
import `in`.iambhvsh.shelf.components.home.NoteEditorSheet
import `in`.iambhvsh.shelf.components.home.PhotoPreview
import `in`.iambhvsh.shelf.components.home.ReminderPickerSheet
import `in`.iambhvsh.shelf.components.home.TagManagerSheet

@Composable
fun CollectionDetailSheets(
    state: CollectionState,
    viewModel: CollectionViewModel,
    context: Context,
    clipboard: Clipboard,
    bookmarkToDelete: Bookmark?,
    onSetBookmarkToDelete: (Bookmark?) -> Unit
) {
    BookmarkPreviewSheet(
        bookmark = state.tempBookmark,
        showBottomSheet = state.isDetailBodySheet,
        isPinned = state.tempBookmark?.isPinned ?: false,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.DismissDetailBodySheet) },
        openInBrowser = {
            state.tempBookmark?.url?.let { openChromeTab(url = it, context = context) }
        },
        copyLinkButtonClick = {
            state.tempBookmark?.url?.let { context.copyToClipboard(it) }
        },
        onShareButtonClick = {
            state.tempBookmark?.url?.let { url ->
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Link"))
            }
        },
        onPinButtonClick = { state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.TogglePin(it)) } },
        onTagsButtonClick = { viewModel.onEvent(CollectionEvents.ShowTagManager) },
        onNoteButtonClick = { viewModel.onEvent(CollectionEvents.ShowNoteEditor(state.tempBookmark?.note)) },
        onReminderButtonClick = { viewModel.onEvent(CollectionEvents.ShowReminderPicker) },
        onRenameButtonClick = {
            state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.ShowEditDialog(it)) }
        },
        onDeleteButtonClick = {
            state.tempBookmark?.let { onSetBookmarkToDelete(it) }
        },
        onPhotoClick = { url ->
            viewModel.onEvent(CollectionEvents.PreviewImageClick(url))
        }
    )

    if (bookmarkToDelete != null) {
        DeleteConfirmationDialog(
            title = "Delete Bookmark",
            text = "Are you sure you want to delete this bookmark? It will be moved to Recently Deleted.",
            onConfirm = {
                viewModel.onEvent(CollectionEvents.DeleteBookmark(bookmarkToDelete))
                onSetBookmarkToDelete(null)
            },
            onDismiss = { onSetBookmarkToDelete(null) }
        )
    }

    val bookmarkToEdit = state.tempBookmark
    if (state.showRenameBookmarkDialog && bookmarkToEdit != null) {
        EditBookmarkSheet(
            bookmark = bookmarkToEdit,
            onDismissRequest = { viewModel.onEvent(CollectionEvents.HideEditDialog) },
            onSaveClick = { newTitle, newDescription ->
                viewModel.onEvent(CollectionEvents.UpdateBookmarkDetails(bookmarkToEdit.id, newTitle, newDescription))
            }
        )
    }

    if (state.showTagManager) {
        TagManagerSheet(
            tags = state.tags,
            selectedTags = state.tempBookmarkTags,
            onToggleTag = { tag, isChecked ->
                viewModel.onEvent(CollectionEvents.ToggleTagForBookmark(tag, isChecked))
            },
            onCreateTag = { name -> viewModel.onEvent(CollectionEvents.CreateTag(name)) },
            onDeleteTag = { tag -> viewModel.onEvent(CollectionEvents.DeleteTag(tag.id)) },
            onDismiss = { viewModel.onEvent(CollectionEvents.HideTagManager) }
        )
    }

    if (state.showNoteEditor) {
        NoteEditorSheet(
            initialNote = state.noteEditorText,
            onSave = { note ->
                state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.UpdateNote(it.id, note)) }
            },
            onDismiss = { viewModel.onEvent(CollectionEvents.HideNoteEditor) }
        )
    }

    if (state.showReminderPicker) {
        ReminderPickerSheet(
            onSetReminder = { timeInMillis ->
                state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.SetReminder(it.id, timeInMillis)) }
            },
            onCancelReminder = {
                state.tempBookmark?.let { viewModel.onEvent(CollectionEvents.CancelReminder(it.id)) }
            },
            hasExistingReminder = state.tempBookmark?.reminderTime != null,
            onDismiss = { viewModel.onEvent(CollectionEvents.HideReminderPicker) }
        )
    }

    HomeInputSheet(
        showBottomSheet = state.showAddBookmarkDialog,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.HideAddBookmarkDialog) },
        value = state.addBookmarkUrl,
        onTextChange = { viewModel.onEvent(CollectionEvents.OnAddBookmarkUrlChange(it)) },
        onSaveClick = { viewModel.onEvent(CollectionEvents.SaveBookmarkInCollection) }
    )

    PhotoPreview(
        isDialog = state.isPhotoPreviewDialog,
        onDismissRequest = { viewModel.onEvent(CollectionEvents.DismissPreviewDialog) },
        imageURL = state.previewPhotoUrl ?: ""
    )
}
