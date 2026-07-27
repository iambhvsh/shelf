package in.iambhvsh.shelf.presentation.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.iambhvsh.shelf.ui.theme.AccentColor

/**
 * Google Material You-style accent color picker: 7 swatches laid out as a
 * 3x3 grid, with the 7th (last) swatch centered alone on the third row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentColorSheet(
    current: AccentColor,
    onSelect: (AccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = "Accent color",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
        )

        val rows = AccentColor.entries.chunked(3)

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            rows.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                ) {
                    rowColors.forEach { accent ->
                        AccentSwatch(
                            accent = accent,
                            selected = accent == current,
                            onClick = { onSelect(accent) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.size(24.dp))
    }
}

@Composable
private fun AccentSwatch(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    val checkTint = if (accent.seed.luminance() > 0.5f) Color.Black else Color.White

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accent.seed, CircleShape)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                )
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    color = checkTint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(
            text = accent.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
