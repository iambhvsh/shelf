package `in`.iambhvsh.shelf

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private var fakeElapsedRealtime = 10_000L

    @Before
    fun setUp() {
        SessionManager.reset()
        SessionManager.elapsedRealtimeProvider = { fakeElapsedRealtime }
    }

    @After
    fun tearDown() {
        SessionManager.reset()
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

    @Test
    fun testImmediateTimeout_relocksOnAnyBackgroundRoundTrip() {
        SessionManager.isUnlocked = true
        SessionManager.onAppBackgrounded()
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 0L)
        assertFalse(SessionManager.isUnlocked)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 0L))
    }

    @Test
    fun testWithinTimeout_preservesUnlockedSession() {
        SessionManager.isUnlocked = true
        SessionManager.onAppBackgrounded()

        fakeElapsedRealtime += 59_999L
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60_000L)
        assertTrue(SessionManager.isUnlocked)

        fakeElapsedRealtime += 1L
        SessionManager.onAppBackgrounded()
        fakeElapsedRealtime += 60_000L
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60_000L)
        assertTrue(SessionManager.isUnlocked)
    }

    @Test
    fun testExceededTimeout_relocksSession() {
        SessionManager.isUnlocked = true
        SessionManager.onAppBackgrounded()

        fakeElapsedRealtime += 60_001L
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60_000L)
        assertFalse(SessionManager.isUnlocked)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60_000L))
    }

    @Test
    fun testTogglingAppLockOff_unlocksEvenAfterPriorLockout() {
        SessionManager.isUnlocked = false
        SessionManager.onAppForegrounded(appLockEnabled = false, timeout = 0L)
        assertTrue(SessionManager.isUnlocked)
        assertFalse(SessionManager.shouldRequireAuthentication(appLockEnabled = false, timeout = 0L))
    }

    @Test
    fun testExactTimeoutBoundary_neverBackgrounded_negativeTimeout_andReset() {
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60_000L)
        assertFalse(SessionManager.isUnlocked)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60_000L))

        SessionManager.isUnlocked = true
        SessionManager.onAppBackgrounded()
        fakeElapsedRealtime += 60_000L
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = 60_000L)
        assertTrue(SessionManager.isUnlocked)
        assertFalse(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60_000L))

        SessionManager.onAppBackgrounded()
        SessionManager.onAppForegrounded(appLockEnabled = true, timeout = -1L)
        assertFalse(SessionManager.isUnlocked)

        SessionManager.isUnlocked = true
        SessionManager.reset()
        assertFalse(SessionManager.isUnlocked)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 60_000L))
    }

    @Test
    fun testImmediateStop_relocksWithoutWaitingForProcessLifecycleDebounce() {
        SessionManager.isUnlocked = true
        SessionManager.onMainActivityStopped(
            appLockEnabled = true,
            timeout = 0L,
            isChangingConfigurations = false,
            isAuthenticating = false
        )
        assertFalse(SessionManager.isUnlocked)
        assertTrue(SessionManager.shouldRequireAuthentication(appLockEnabled = true, timeout = 0L))
    }

    @Test
    fun testImmediateStop_ignoresConfigChangesAndActiveAuthentication() {
        SessionManager.isUnlocked = true
        SessionManager.onMainActivityStopped(
            appLockEnabled = true,
            timeout = 0L,
            isChangingConfigurations = true,
            isAuthenticating = false
        )
        assertTrue(SessionManager.isUnlocked)

        SessionManager.onMainActivityStopped(
            appLockEnabled = true,
            timeout = 0L,
            isChangingConfigurations = false,
            isAuthenticating = true
        )
        assertTrue(SessionManager.isUnlocked)
    }

    @Test
    fun testQuickSaveAccess_blocksWhenLockedAndOverlayDoesNotExtendBackgroundTimer() {
        assertFalse(SessionManager.canAccessQuickSave(appLockEnabled = true, timeout = 60_000L))

        SessionManager.isUnlocked = true
        SessionManager.onMainActivityStopped(
            appLockEnabled = true,
            timeout = 60_000L,
            isChangingConfigurations = false,
            isAuthenticating = false
        )
        fakeElapsedRealtime += 20_000L
        assertTrue(SessionManager.canAccessQuickSave(appLockEnabled = true, timeout = 60_000L))

        SessionManager.onProcessStopped(hasMainActivityForeground = false)
        fakeElapsedRealtime += 45_000L
        assertFalse(SessionManager.canAccessQuickSave(appLockEnabled = true, timeout = 60_000L))
        assertFalse(SessionManager.isUnlocked)
    }

    @Test
    fun testQuickSaveAccess_allowsWhenAppLockDisabled() {
        SessionManager.isUnlocked = false
        assertTrue(SessionManager.canAccessQuickSave(appLockEnabled = false, timeout = 0L))
    }

    @Test
    fun testResolveBiometricAvailability_keepsLockOnTemporaryHardwareUnavailable() {
        assertEquals(
            BiometricAvailability.AVAILABLE,
            resolveBiometricAvailability(androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS)
        )
        assertEquals(
            BiometricAvailability.TEMPORARILY_UNAVAILABLE,
            resolveBiometricAvailability(androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        )
        assertEquals(
            BiometricAvailability.TEMPORARILY_UNAVAILABLE,
            resolveBiometricAvailability(androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED)
        )
        assertEquals(
            BiometricAvailability.UNSUPPORTED_OR_NOT_ENROLLED,
            resolveBiometricAvailability(androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        )
        assertEquals(
            BiometricAvailability.UNSUPPORTED_OR_NOT_ENROLLED,
            resolveBiometricAvailability(androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)
        )
    }
}
