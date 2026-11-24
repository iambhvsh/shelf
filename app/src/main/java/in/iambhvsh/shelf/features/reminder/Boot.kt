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
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        scope.launch {
            try {
                val now = System.currentTimeMillis()
                val bookmarks = bookmarkDao.getAllBookmarksOnce()
                for (bm in bookmarks) {
                    val time = bm.reminderTime
                    if (time != null && time > now && !bm.isHidden) {
                        reminderManager.scheduleReminder(bm.id, time)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
