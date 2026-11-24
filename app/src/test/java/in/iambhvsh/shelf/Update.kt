package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.UpdateManagerImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTest {

    @Test
    fun testIsVersionNewer_higherMajorVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("1.0.0", "2.0.0"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "3.0.0"))
    }

    @Test
    fun testIsVersionNewer_higherMinorVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.1.0"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.0", "2.0.1"))
    }

    @Test
    fun testIsVersionNewer_higherPatchVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.10"))
    }

    @Test
    fun testIsVersionNewer_equalOrLowerVersionReturnsFalse() {
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.4"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.3"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.1.0", "2.0.9"))
        assertFalse(UpdateManagerImpl.isVersionNewer("3.0.0", "2.9.9"))
    }

    @Test
    fun testIsVersionNewer_handlesTagPrefixAndPrereleaseSuffix() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("v2.0.4", "2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.5-beta"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.4-beta"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.5", "v2.0.4-beta"))
    }

    @Test
    fun testIsVersionNewer_handlesDifferentSegmentCounts() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0", "2.0.1"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.0", "2.0.0.1"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.1", "2.0"))
    }
}
