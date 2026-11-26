package `in`.iambhvsh.shelf.features.reminder

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
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val bookmarkDao: BookmarkDao by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val bookmarkId = intent.getLongExtra(ReminderManager.EXTRA_BOOKMARK_ID, -1L)
        if (bookmarkId == -1L) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val bookmark = runCatching { bookmarkDao.getBookmarkById(bookmarkId) }.getOrNull()
                runCatching { bookmarkDao.updateReminderTime(bookmarkId, null) }
                if (bookmark == null || bookmark.isHidden) return@launch

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Bookmark Reminders",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val activityIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("OPEN_BOOKMARK_ID", bookmarkId)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    bookmarkId.toInt(),
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val message = bookmark.title?.takeIf { it.isNotBlank() }
                    ?: bookmark.url.takeIf { it.isNotBlank() }
                    ?: "Don't forget to read your saved bookmark!"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Shelf Reminder")
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                try {
                    notificationManager.notify(bookmarkId.toInt(), notification)
                } catch (_: SecurityException) {
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "shelf_reminders"
    }
}
