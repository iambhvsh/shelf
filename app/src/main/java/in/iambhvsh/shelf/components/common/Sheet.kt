package `in`.iambhvsh.shelf.components.common

import android.content.ClipData
import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberShelfSheetState(skipPartiallyExpanded: Boolean = true): SheetState {
    return rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = if (skipPartiallyExpanded) {
            setOf(SheetValue.Hidden, SheetValue.Expanded)
        } else {
            setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded)
        }
    )
}

fun Context.copyToClipboard(text: String, label: String = "") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}
