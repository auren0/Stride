package com.example.stride

import com.example.stride.data.counterDelta
import com.example.stride.data.legacyDate
import com.example.stride.data.wasRebooted
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StepMathTest {
    @Test fun firstReadingDoesNotImportEarlierSteps() {
        assertEquals(0, counterDelta(null, 12000, false))
        assertEquals(0, counterDelta(null, 100000, true))
    }
    @Test fun cumulativeReadingsCountOnlyNewSteps() {
        assertEquals(15, counterDelta(12000, 12015, false))
        assertEquals(0, counterDelta(12015, 12015, false))
    }
    @Test fun rebootCountsStepsTakenSinceBoot() {
        assertEquals(800, counterDelta(100, 800, true))
        assertEquals(4, counterDelta(100, 4, false)) // counter went backwards => restarted
    }
    @Test fun largeCumulativeValueIsNeverAddedWithoutReboot() {
        // 100 000 on the sensor, previous 99 950 => only 50 new steps, not 100 000.
        assertEquals(50, counterDelta(99_950, 100_000, false))
    }
    @Test fun rebootDetectionUsesUptimeNotWallClock() {
        assertFalse(wasRebooted(null, 5_000))
        assertFalse(wasRebooted(1_000, 5_000))
        assertTrue(wasRebooted(5_000_000, 60_000))
    }
    @Test fun migratesZeroBasedMonthsAndRejectsInvalidDates() {
        assertEquals(LocalDate.of(2026, 1, 31), legacyDate("2026-0-31"))
        assertEquals(LocalDate.of(2026, 12, 1), legacyDate("2026-11-1"))
        assertEquals(LocalDate.of(2024, 2, 29), legacyDate("2024-1-29"))
        assertNull(legacyDate("2026-1-29"))
        assertNull(legacyDate("broken"))
    }
}
