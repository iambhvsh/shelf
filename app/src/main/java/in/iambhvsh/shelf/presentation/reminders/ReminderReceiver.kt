package `in`.iambhvsh.shelf.presentation.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import `in`.iambhvsh.shelf.MainActivity
import `in`.iambhvsh.shelf.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bookmarkId = intent.getLongExtra(ReminderManager.EXTRA_BOOKMARK_ID, -1L)
        if (bookmarkId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bookmark Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Deep link intent to open the app (we will handle the deep link in MainActivity)
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_BOOKMARK_ID", bookmarkId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            bookmarkId.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.bookmark_fill) // using existing bookmark icon
            .setContentTitle("Shelf Reminder")
            .setContentText("Don't forget to read your saved bookmark!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookmarkId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "shelf_reminders"
    }
}
