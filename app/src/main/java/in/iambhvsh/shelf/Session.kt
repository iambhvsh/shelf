package `in`.iambhvsh.shelf

import android.os.SystemClock

object SessionManager {
    private var lastBackgroundTime: Long? = null
    var isUnlocked: Boolean = false

    fun onAppBackgrounded() {
        lastBackgroundTime = SystemClock.elapsedRealtime()
    }

    fun onAppForegrounded(appLockEnabled: Boolean, timeout: Long) {
        if (!appLockEnabled) {
            isUnlocked = true
            return
        }

        val backgroundTime = lastBackgroundTime
        if (backgroundTime == null) {
            // Fresh launch or killed in background
            isUnlocked = false
        } else {
            val elapsed = SystemClock.elapsedRealtime() - backgroundTime
            if (elapsed > timeout) {
                isUnlocked = false
            }
        }
    }

    fun shouldRequireAuthentication(appLockEnabled: Boolean, timeout: Long): Boolean {
        if (!appLockEnabled) return false
        return !isUnlocked
    }
}
