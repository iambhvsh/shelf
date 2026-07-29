package `in`.iambhvsh.shelf.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerSheet(
    onSetReminder: (Long) -> Unit,
    onCancelReminder: () -> Unit,
    hasExistingReminder: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = screenHeight * 0.9f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Set Reminder",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 20) // 8 PM
                        cal.set(Calendar.MINUTE, 0)
                        if (cal.timeInMillis < System.currentTimeMillis()) {
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        onSetReminder(cal.timeInMillis)
                    },
                headlineContent = { Text("Later Today (8:00 PM)") },
                leadingContent = { Icon(Icons.Outlined.NightsStay, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )

            Spacer(Modifier.height(8.dp))

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 9) // 9 AM
                        cal.set(Calendar.MINUTE, 0)
                        onSetReminder(cal.timeInMillis)
                    },
                headlineContent = { Text("Tomorrow Morning (9:00 AM)") },
                leadingContent = { Icon(Icons.Outlined.WbSunny, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )

            Spacer(Modifier.height(8.dp))

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        val cal = Calendar.getInstance()
                        // Move to next Saturday
                        val daysUntilSaturday = (Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
                        cal.add(Calendar.DAY_OF_YEAR, if (daysUntilSaturday == 0) 7 else daysUntilSaturday)
                        cal.set(Calendar.HOUR_OF_DAY, 10) // 10 AM
                        cal.set(Calendar.MINUTE, 0)
                        onSetReminder(cal.timeInMillis)
                    },
                headlineContent = { Text("This Weekend (Sat 10:00 AM)") },
                leadingContent = { Icon(Icons.Outlined.Event, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )

            if (hasExistingReminder) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onCancelReminder,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove Reminder")
                }
            }
        }
    }
}
