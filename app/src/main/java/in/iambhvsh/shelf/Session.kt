package `in`.iambhvsh.shelf

import android.os.SystemClock

object SessionManager {
    private var lastBackgroundTime: Long? = null
    var isUnlocked: Boolean = false
    internal var elapsedRealtimeProvider: () -> Long = { SystemClock.elapsedRealtime() }

    fun onAppBackgrounded() {
        lastBackgroundTime = elapsedRealtimeProvider()
    }

    fun onAppForegrounded(appLockEnabled: Boolean, timeout: Long) {
        if (!appLockEnabled) {
            isUnlocked = true
            return
        }

        val backgroundTime = lastBackgroundTime
        if (backgroundTime == null) {
            isUnlocked = false
        } else {
            val elapsed = elapsedRealtimeProvider() - backgroundTime
            if (timeout <= 0L || elapsed > timeout) {
                isUnlocked = false
            }
        }
    }

    fun shouldRequireAuthentication(appLockEnabled: Boolean, timeout: Long): Boolean {
        if (!appLockEnabled) return false
        return !isUnlocked
    }

    internal fun reset() {
        lastBackgroundTime = null
        isUnlocked = false
        elapsedRealtimeProvider = { SystemClock.elapsedRealtime() }
    }
}
