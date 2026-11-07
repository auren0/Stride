package com.example.stride

import com.example.stride.data.counterDelta
import com.example.stride.data.legacyDate
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StepMathTest {
    @Test fun firstReadingDoesNotImportEarlierSteps() {
        assertEquals(0, counterDelta(null, 12000, true))
    }
    @Test fun cumulativeReadingsCountOnlyNewSteps() {
        assertEquals(15, counterDelta(12000, 12015, true))
        assertEquals(0, counterDelta(12015, 12015, true))
    }
    @Test fun rebootCountsNewBootEvenWhenCounterExceedsOldValue() {
        assertEquals(800, counterDelta(100, 800, false))
        assertEquals(4, counterDelta(100, 4, true))
    }
    @Test fun migratesZeroBasedMonthsAndRejectsInvalidDates() {
        assertEquals(LocalDate.of(2026, 1, 31), legacyDate("2026-0-31"))
        assertEquals(LocalDate.of(2026, 12, 1), legacyDate("2026-11-1"))
        assertEquals(LocalDate.of(2024, 2, 29), legacyDate("2024-1-29"))
        assertNull(legacyDate("2026-1-29"))
        assertNull(legacyDate("broken"))
    }
}
