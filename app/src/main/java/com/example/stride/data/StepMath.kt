package com.example.stride.data

import java.time.LocalDate

/** Old versions stored Calendar.MONTH (zero based) without padding. */
fun legacyDate(value: String): LocalDate? = runCatching {
    val parts = value.split("-").map(String::toInt)
    LocalDate.of(parts[0], parts[1] + 1, parts[2])
}.getOrNull()

fun counterDelta(previous: Int?, current: Int, sameBoot: Boolean): Int = when {
    previous == null -> 0 // Never import steps taken before tracking began.
    !sameBoot || current < previous -> current.coerceAtLeast(0)
    else -> current - previous
}
