package `in`.iambhvsh.shelf.features.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val bookmarkDao: BookmarkDao by inject()
    private val reminderManager: ReminderManager by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pendingResult = goAsync()
        scope.launch {
            try {
                val bookmarks = bookmarkDao.getAllBookmarksOnce()
                reschedulePendingReminders(
                    bookmarks = bookmarks,
                    nowMillis = System.currentTimeMillis(),
                    schedule = { id, time -> reminderManager.scheduleReminder(id, time) }
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}

internal inline fun reschedulePendingReminders(
    bookmarks: List<`in`.iambhvsh.shelf.data.BookmarkEntity>,
    nowMillis: Long,
    schedule: (Long, Long) -> Unit
): Int {
    var scheduledCount = 0
    for (bm in bookmarks) {
        val time = bm.reminderTime
        if (time != null && time > nowMillis && !bm.isHidden) {
            schedule(bm.id, time)
            scheduledCount++
        }
    }
    return scheduledCount
}
