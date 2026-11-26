package `in`.iambhvsh.shelf

import android.os.SystemClock

object SessionManager {
    private var lastBackgroundTime: Long? = null
    var isUnlocked: Boolean = false
    internal var elapsedRealtimeProvider: () -> Long = { SystemClock.elapsedRealtime() }

    fun onAppBackgrounded() {
        lastBackgroundTime = elapsedRealtimeProvider()
    }

    fun onProcessStopped(hasMainActivityForeground: Boolean) {
        if (hasMainActivityForeground || lastBackgroundTime == null) {
            lastBackgroundTime = elapsedRealtimeProvider()
        }
    }

    fun onMainActivityStopped(
        appLockEnabled: Boolean,
        timeout: Long,
        isChangingConfigurations: Boolean,
        isAuthenticating: Boolean
    ) {
        if (!appLockEnabled || isChangingConfigurations || isAuthenticating) return
        lastBackgroundTime = elapsedRealtimeProvider()
        if (timeout <= 0L) {
            isUnlocked = false
        }
    }

    fun canAccessQuickSave(appLockEnabled: Boolean, timeout: Long): Boolean {
        if (!appLockEnabled) return true
        if (!isUnlocked) return false
        val backgroundTime = lastBackgroundTime
        if (backgroundTime != null) {
            val elapsed = elapsedRealtimeProvider() - backgroundTime
            if (timeout <= 0L || elapsed > timeout) {
                isUnlocked = false
                return false
            }
        }
        return true
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
