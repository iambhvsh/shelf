@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import `in`.iambhvsh.shelf.components.common.ExpressiveTextButton
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.components.common.rememberShelfSheetState
import `in`.iambhvsh.shelf.theme.ShelfShapes
import java.util.Calendar
import java.util.TimeZone

internal fun localTodayUtcMidnightMillis(
    nowMillis: Long = System.currentTimeMillis(),
    localTimeZone: TimeZone = TimeZone.getDefault()
): Long {
    val localCal = Calendar.getInstance(localTimeZone).apply {
        timeInMillis = nowMillis
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(Calendar.YEAR, localCal.get(Calendar.YEAR))
        set(Calendar.MONTH, localCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

internal fun combineUtcDateWithLocalTime(
    utcDateMillis: Long,
    hour: Int,
    minute: Int,
    targetTimeZone: TimeZone = TimeZone.getDefault()
): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcDateMillis
    }
    return Calendar.getInstance(targetTimeZone).apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun computeLaterTodayReminder(
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val cal = Calendar.getInstance(timeZone).apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (cal.timeInMillis <= nowMillis) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
}

internal fun computeTomorrowMorningReminder(
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    return Calendar.getInstance(timeZone).apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun computeWeekendReminder(
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val cal = Calendar.getInstance(timeZone).apply {
        timeInMillis = nowMillis
    }
    val daysUntilSaturday = (Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
    cal.add(Calendar.DAY_OF_YEAR, if (daysUntilSaturday == 0) 7 else daysUntilSaturday)
    cal.set(Calendar.HOUR_OF_DAY, 10)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReminderPickerSheet(
    onSetReminder: (Long) -> Unit,
    onCancelReminder: () -> Unit,
    hasExistingReminder: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberShelfSheetState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localTodayUtcMidnightMillis()
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
                            onSetReminder(
                                combineUtcDateWithLocalTime(
                                    utcDateMillis = selectedDate,
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute
                                )
                            )
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)) {
                SegmentedListItem(
                    onClick = { onSetReminder(computeLaterTodayReminder()) },
                    shapes = ShelfShapes.segmented(0, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("Later Today (8:00 PM)") },
                    leadingContent = { Icon(Icons.Outlined.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )

                SegmentedListItem(
                    onClick = { onSetReminder(computeTomorrowMorningReminder()) },
                    shapes = ShelfShapes.segmented(1, 4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressiveBounce(scaleDown = 0.98f),
                    content = { Text("Tomorrow Morning (9:00 AM)") },
                    leadingContent = { Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )

                SegmentedListItem(
                    onClick = { onSetReminder(computeWeekendReminder()) },
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
