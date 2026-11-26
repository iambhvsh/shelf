package `in`.iambhvsh.shelf

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.iambhvsh.shelf.data.SettingsRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.getKoin

@RunWith(AndroidJUnit4::class)
class DeviceIntegrationTest {

    private lateinit var settingsRepository: SettingsRepository
    private var prevAppLock = false

    @Before
    fun setUp() {
        settingsRepository = getKoin().get()
        prevAppLock = settingsRepository.getAppLockEnabled()
        SessionManager.reset()
    }

    @After
    fun tearDown() {
        settingsRepository.setAppLockEnabled(prevAppLock)
        SessionManager.reset()
    }

    @Test
    fun quickSaveActivity_blocksWhenLockedAndRejectsPlainText() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        settingsRepository.setAppLockEnabled(true)
        SessionManager.isUnlocked = false
        val lockedIntent = Intent(context, QuickSaveActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://blocked-when-locked.example.com")
        }
        ActivityScenario.launch<QuickSaveActivity>(lockedIntent).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }

        settingsRepository.setAppLockEnabled(false)
        SessionManager.isUnlocked = true
        val plainTextIntent = Intent(context, QuickSaveActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Meeting at 5pm without any link")
        }
        ActivityScenario.launch<QuickSaveActivity>(plainTextIntent).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun biometricAvailabilityMapping_preservesLockSemantics() {
        assertEquals(
            BiometricAvailability.TEMPORARILY_UNAVAILABLE,
            resolveBiometricAvailability(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        )
        assertTrue(
            resolveBiometricAvailability(BiometricManager.BIOMETRIC_SUCCESS) == BiometricAvailability.AVAILABLE
        )
    }
}
