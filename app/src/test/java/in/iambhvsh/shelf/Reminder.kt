package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.components.home.combineUtcDateWithLocalTime
import `in`.iambhvsh.shelf.components.home.computeLaterTodayReminder
import `in`.iambhvsh.shelf.components.home.computeTomorrowMorningReminder
import `in`.iambhvsh.shelf.components.home.computeWeekendReminder
import `in`.iambhvsh.shelf.components.home.localTodayUtcMidnightMillis
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.features.reminder.reschedulePendingReminders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderTest {

    @Test
    fun testCombineUtcDateWithLocalTime_preservesSelectedCalendarDateInNegativeTimezone() {
        val nyZone = TimeZone.getTimeZone("America/New_York")
        val selectedUtcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.MARCH, 15, 0, 0, 0)
        }.timeInMillis

        val resultMillis = combineUtcDateWithLocalTime(
            utcDateMillis = selectedUtcMidnight,
            hour = 9,
            minute = 30,
            targetTimeZone = nyZone
        )

        val local = Calendar.getInstance(nyZone).apply { timeInMillis = resultMillis }
        assertEquals(2026, local.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, local.get(Calendar.MONTH))
        assertEquals(15, local.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, local.get(Calendar.MINUTE))
        assertEquals(0, local.get(Calendar.SECOND))
        assertEquals(0, local.get(Calendar.MILLISECOND))
    }

    @Test
    fun testCombineUtcDateWithLocalTime_preservesSelectedDateInPositiveHalfHourTimezone() {
        val istZone = TimeZone.getTimeZone("Asia/Kolkata")
        val selectedUtcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.DECEMBER, 31, 0, 0, 0)
        }.timeInMillis

        val resultMillis = combineUtcDateWithLocalTime(
            utcDateMillis = selectedUtcMidnight,
            hour = 22,
            minute = 15,
            targetTimeZone = istZone
        )

        val local = Calendar.getInstance(istZone).apply { timeInMillis = resultMillis }
        assertEquals(2026, local.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, local.get(Calendar.MONTH))
        assertEquals(31, local.get(Calendar.DAY_OF_MONTH))
        assertEquals(22, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, local.get(Calendar.MINUTE))
        assertEquals(0, local.get(Calendar.SECOND))
        assertEquals(0, local.get(Calendar.MILLISECOND))
    }

    @Test
    fun testLocalTodayUtcMidnightMillis_usesLocalDateNearMidnightAcrossTimezones() {
        val laZone = TimeZone.getTimeZone("America/Los_Angeles")
        val laLateNightMillis = Calendar.getInstance(laZone).apply {
            clear()
            set(2026, Calendar.APRIL, 10, 22, 45, 12)
        }.timeInMillis

        val utcMidnight = localTodayUtcMidnightMillis(nowMillis = laLateNightMillis, localTimeZone = laZone)
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnight }

        assertEquals(2026, utcCal.get(Calendar.YEAR))
        assertEquals(Calendar.APRIL, utcCal.get(Calendar.MONTH))
        assertEquals(10, utcCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, utcCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, utcCal.get(Calendar.MINUTE))
    }

    @Test
    fun testComputeLaterTodayReminder_beforeAndAfterEveningCutoff() {
        val utc = TimeZone.getTimeZone("UTC")
        val afternoon = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.JUNE, 2, 14, 23, 45)
            set(Calendar.MILLISECOND, 321)
        }.timeInMillis

        val sameDayEvening = computeLaterTodayReminder(afternoon, utc)
        val calSame = Calendar.getInstance(utc).apply { timeInMillis = sameDayEvening }
        assertEquals(2, calSame.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, calSame.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calSame.get(Calendar.MINUTE))
        assertEquals(0, calSame.get(Calendar.SECOND))
        assertEquals(0, calSame.get(Calendar.MILLISECOND))

        val night = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.JUNE, 2, 20, 0, 0)
        }.timeInMillis
        val nextDayEvening = computeLaterTodayReminder(night, utc)
        val calNext = Calendar.getInstance(utc).apply { timeInMillis = nextDayEvening }
        assertEquals(3, calNext.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, calNext.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testComputeTomorrowAndWeekendReminders_crossYearAndZeroSubMinuteFields() {
        val utc = TimeZone.getTimeZone("UTC")
        val dec31 = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.DECEMBER, 31, 11, 42, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val tomorrow = Calendar.getInstance(utc).apply {
            timeInMillis = computeTomorrowMorningReminder(dec31, utc)
        }
        assertEquals(2027, tomorrow.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, tomorrow.get(Calendar.MONTH))
        assertEquals(1, tomorrow.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, tomorrow.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, tomorrow.get(Calendar.SECOND))
        assertEquals(0, tomorrow.get(Calendar.MILLISECOND))

        val saturday = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.JUNE, 6, 8, 0, 0)
        }.timeInMillis
        val nextSaturday = Calendar.getInstance(utc).apply {
            timeInMillis = computeWeekendReminder(saturday, utc)
        }
        assertEquals(Calendar.SATURDAY, nextSaturday.get(Calendar.DAY_OF_WEEK))
        assertEquals(13, nextSaturday.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, nextSaturday.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, nextSaturday.get(Calendar.SECOND))
    }

    @Test
    fun testReschedulePendingReminders_skipsHiddenAndExpiredReminders() {
        val now = 1_000_000L
        val bookmarks = listOf(
            BookmarkEntity(id = 1L, url = "https://1.com", title = "A", description = null, imageUrl = null, reminderTime = now + 5000L, isHidden = false),
            BookmarkEntity(id = 2L, url = "https://2.com", title = "B", description = null, imageUrl = null, reminderTime = now + 5000L, isHidden = true),
            BookmarkEntity(id = 3L, url = "https://3.com", title = "C", description = null, imageUrl = null, reminderTime = now - 1L, isHidden = false),
            BookmarkEntity(id = 4L, url = "https://4.com", title = "D", description = null, imageUrl = null, reminderTime = now, isHidden = false),
            BookmarkEntity(id = 5L, url = "https://5.com", title = "E", description = null, imageUrl = null, reminderTime = null, isHidden = false)
        )

        val scheduled = mutableListOf<Pair<Long, Long>>()
        val count = reschedulePendingReminders(bookmarks, now) { id, time ->
            scheduled.add(id to time)
        }

        assertEquals(1, count)
        assertEquals(listOf(1L to (now + 5000L)), scheduled)
        assertTrue(scheduled.all { it.first == 1L })
    }

    @Test
    fun testDstSpringForwardAndFallBack_preserveLocalWallClockHours() {
        val nyZone = TimeZone.getTimeZone("America/New_York")
        val utcZone = TimeZone.getTimeZone("UTC")

        val springForwardUtc = Calendar.getInstance(utcZone).apply {
            clear()
            set(2026, Calendar.MARCH, 8, 0, 0, 0)
        }.timeInMillis
        val springResult = combineUtcDateWithLocalTime(springForwardUtc, hour = 9, minute = 15, targetTimeZone = nyZone)
        val springLocal = Calendar.getInstance(nyZone).apply { timeInMillis = springResult }
        assertEquals(Calendar.MARCH, springLocal.get(Calendar.MONTH))
        assertEquals(8, springLocal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, springLocal.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, springLocal.get(Calendar.MINUTE))

        val fallBackUtc = Calendar.getInstance(utcZone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 0, 0, 0)
        }.timeInMillis
        val fallResult = combineUtcDateWithLocalTime(fallBackUtc, hour = 8, minute = 45, targetTimeZone = nyZone)
        val fallLocal = Calendar.getInstance(nyZone).apply { timeInMillis = fallResult }
        assertEquals(Calendar.NOVEMBER, fallLocal.get(Calendar.MONTH))
        assertEquals(1, fallLocal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, fallLocal.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, fallLocal.get(Calendar.MINUTE))
    }

    @Test
    fun testComputeTomorrowAndWeekendReminders_leapDayAndWeekdayVariants() {
        val utc = TimeZone.getTimeZone("UTC")
        val leapDay = Calendar.getInstance(utc).apply {
            clear()
            set(2024, Calendar.FEBRUARY, 29, 15, 30, 0)
        }.timeInMillis
        val nextDay = Calendar.getInstance(utc).apply {
            timeInMillis = computeTomorrowMorningReminder(leapDay, utc)
        }
        assertEquals(2024, nextDay.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, nextDay.get(Calendar.MONTH))
        assertEquals(1, nextDay.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, nextDay.get(Calendar.HOUR_OF_DAY))

        val friday = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.JUNE, 5, 18, 0, 0)
        }.timeInMillis
        val satFromFri = Calendar.getInstance(utc).apply {
            timeInMillis = computeWeekendReminder(friday, utc)
        }
        assertEquals(Calendar.SATURDAY, satFromFri.get(Calendar.DAY_OF_WEEK))
        assertEquals(6, satFromFri.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, satFromFri.get(Calendar.HOUR_OF_DAY))

        val sunday = Calendar.getInstance(utc).apply {
            clear()
            set(2026, Calendar.JUNE, 7, 12, 0, 0)
        }.timeInMillis
        val satFromSun = Calendar.getInstance(utc).apply {
            timeInMillis = computeWeekendReminder(sunday, utc)
        }
        assertEquals(Calendar.SATURDAY, satFromSun.get(Calendar.DAY_OF_WEEK))
        assertEquals(13, satFromSun.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testReminderManager_handlesNullAlarmManagerAndPastTimestampsSafely() {
        val manager = `in`.iambhvsh.shelf.features.reminder.ReminderManager(FakeContext())
        manager.scheduleReminder(1L, System.currentTimeMillis() - 10_000L)
        manager.scheduleReminder(1L, System.currentTimeMillis() + 60_000L)
        manager.cancelReminder(1L)

        assertEquals(0, reschedulePendingReminders(emptyList(), 1000L) { _, _ -> })
    }
}
