package `in`.iambhvsh.shelf.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import `in`.iambhvsh.shelf.R

class InstallationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_INTENT,
                    Intent::class.java
                )
                if (confirmationIntent != null) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val inForeground = runCatching {
                        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                    }.getOrDefault(false)

                    val started = if (inForeground) {
                        runCatching { context.startActivity(confirmationIntent) }.isSuccess
                    } else {
                        false
                    }
                    if (!started) {
                        postInstallConfirmationNotification(context, confirmationIntent)
                    }
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                cleanupUpdateApk(context)
                Toast.makeText(context, "Update installed successfully", Toast.LENGTH_SHORT).show()
            }
            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID,
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                cleanupUpdateApk(context)
                Toast.makeText(context, "Update failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cleanupUpdateApk(context: Context) {
        runCatching {
            val file = java.io.File(
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
                "shelf_update.apk"
            )
            if (file.exists()) file.delete()
        }
    }

    private fun postInstallConfirmationNotification(context: Context, confirmationIntent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            UPDATE_NOTIFICATION_ID,
            confirmationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Shelf Update Ready")
            .setContentText("Tap to finish installing the update")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }

    companion object {
        private const val UPDATE_CHANNEL_ID = "shelf_updates"
        private const val UPDATE_NOTIFICATION_ID = 9001
    }
}
