@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.ui.components.ExpressiveButton
import `in`.iambhvsh.shelf.ui.components.ExpressiveTextButton
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReminderPickerSheet(
    onSetReminder: (Long) -> Unit,
    onCancelReminder: () -> Unit,
    hasExistingReminder: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = MaterialTheme.shapes.extraLargeIncreased,
            confirmButton = {
                ExpressiveTextButton(
                    onClick = {
                        showDatePicker = false
                        if (datePickerState.selectedDateMillis != null) {
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                ExpressiveTextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            shape = MaterialTheme.shapes.extraLargeIncreased,
            confirmButton = {
                ExpressiveTextButton(
                    onClick = {
                        showTimePicker = false
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            onSetReminder(cal.timeInMillis)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                ExpressiveTextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = screenHeight * 0.9f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Set Reminder",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)) {
                SegmentedListItem(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 20) // 8 PM
                        cal.set(Calendar.MINUTE, 0)
                        if (cal.timeInMillis < System.currentTimeMillis()) {
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        onSetReminder(cal.timeInMillis)
                    },
                    shapes = ShelfShapes.segmented(0, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("Later Today (8:00 PM)") },
                    leadingContent = { Icon(Icons.Outlined.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )

                SegmentedListItem(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 9) // 9 AM
                        cal.set(Calendar.MINUTE, 0)
                        onSetReminder(cal.timeInMillis)
                    },
                    shapes = ShelfShapes.segmented(1, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("Tomorrow Morning (9:00 AM)") },
                    leadingContent = { Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )

                SegmentedListItem(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val daysUntilSaturday = (Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
                        cal.add(Calendar.DAY_OF_YEAR, if (daysUntilSaturday == 0) 7 else daysUntilSaturday)
                        cal.set(Calendar.HOUR_OF_DAY, 10) // 10 AM
                        cal.set(Calendar.MINUTE, 0)
                        onSetReminder(cal.timeInMillis)
                    },
                    shapes = ShelfShapes.segmented(2, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("This Weekend (Sat 10:00 AM)") },
                    leadingContent = { Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )

                SegmentedListItem(
                    onClick = { showDatePicker = true },
                    shapes = ShelfShapes.segmented(3, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("Custom Date & Time") },
                    leadingContent = { Icon(Icons.Outlined.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            }

            if (hasExistingReminder) {
                Spacer(Modifier.height(20.dp))
                ExpressiveButton(
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
