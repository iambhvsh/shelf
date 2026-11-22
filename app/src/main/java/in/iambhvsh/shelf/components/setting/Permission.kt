@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.iambhvsh.shelf.components.setting

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import `in`.iambhvsh.shelf.components.common.rememberShelfSheetState
import `in`.iambhvsh.shelf.theme.ShelfShapes
import kotlinx.coroutines.launch

@Composable
fun PermissionsSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberShelfSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var hasNotifications by remember { mutableStateOf(checkNotifications(context)) }
    var hasExactAlarm by remember { mutableStateOf(checkExactAlarm(context)) }
    var isIgnoringBattery by remember { mutableStateOf(checkBattery(context)) }
    var canInstallPackages by remember { mutableStateOf(checkInstallPackages(context)) }

    fun refresh() {
        hasNotifications = checkNotifications(context)
        hasExactAlarm = checkExactAlarm(context)
        isIgnoringBattery = checkBattery(context)
        canInstallPackages = checkInstallPackages(context)
    }

    val animateDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotifications = isGranted
            refresh()
        }
    )

    val allGranted = hasNotifications && hasExactAlarm && isIgnoringBattery && canInstallPackages
    val missingCount = listOf(hasNotifications, hasExactAlarm, isIgnoringBattery, canInstallPackages).count { !it }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = animateDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Permissions for Shelf",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Allow permissions to ensure bookmark reminders, background sync, and updates work smoothly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
            ) {
                PermissionItem(
                    title = "Notifications",
                    description = "For bookmark reminders and update alerts",
                    icon = Icons.Outlined.Notifications,
                    isGranted = hasNotifications,
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            launchNotificationSettings(context)
                        }
                    },
                    index = 0,
                    count = 4
                )

                PermissionItem(
                    title = "Exact Alarms",
                    description = "Triggers reminders at the scheduled time",
                    icon = Icons.Outlined.Alarm,
                    isGranted = hasExactAlarm,
                    onGrant = { launchExactAlarmSettings(context) },
                    index = 1,
                    count = 4
                )

                PermissionItem(
                    title = "Background Reliability",
                    description = "Prevents system sleep from silencing reminders",
                    icon = Icons.Outlined.BatteryStd,
                    isGranted = isIgnoringBattery,
                    onGrant = { launchBatterySettings(context) },
                    index = 2,
                    count = 4
                )

                PermissionItem(
                    title = "Install Updates",
                    description = "Allows in-app updates to new Shelf releases",
                    icon = Icons.Outlined.SystemUpdate,
                    isGranted = canInstallPackages,
                    onGrant = { launchInstallSettings(context) },
                    index = 3,
                    count = 4
                )
            }

            Spacer(Modifier.height(24.dp))

            if (!allGranted) {
                Text(
                    text = "$missingCount permission${if (missingCount == 1) "" else "s"} optional or pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }

            ExpressiveButton(
                onClick = animateDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (allGranted) "Everything is Ready!" else "Continue",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
