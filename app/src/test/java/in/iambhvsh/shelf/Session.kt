package `in`.iambhvsh.shelf

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    @Before
    fun setUp() {
        SessionManager.isUnlocked = false
    }

    @Test
    fun testAppLockDisabled_doesNotRequireAuthentication() {
        SessionManager.onAppForegrounded(appLockEnabled = false, timeout = 60000L)
        assertFalse(SessionManager.shouldRequireAuthentication(appLockEnabled = false, timeout = 60000L))
        assertTrue(SessionManager.isUnlocked)
    }

    @Test
    fun testAppLockEnabled_requiresAuthenticationInitially() {
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60000L)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60000L))
        assertFalse(SessionManager.isUnlocked)
    }

    @Test
    fun testManualUnlock_clearsAuthenticationRequirement() {
        SessionManager.isUnlocked = true
        assertFalse(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60000L))
    }

    @Test
    fun testAppLockDisabled_alwaysReturnsFalse() {
        SessionManager.isUnlocked = false
        assertFalse(SessionManager.shouldRequireAuthentication(appLockEnabled = false, timeout = 0L))
    }
}
