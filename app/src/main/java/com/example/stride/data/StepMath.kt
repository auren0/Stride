package com.example.stride.data

import java.time.LocalDate

/** Old versions stored Calendar.MONTH (zero based) without padding. */
fun legacyDate(value: String): LocalDate? = runCatching {
    val parts = value.split("-").map(String::toInt)
    LocalDate.of(parts[0], parts[1] + 1, parts[2])
}.getOrNull()

/**
 * Приращение шагов по показаниям накопительного датчика (TYPE_STEP_COUNTER).
 *
 * Датчик считает шаги с момента загрузки телефона и в полночь не сбрасывается,
 * поэтому в «сегодня» нужно добавлять только разницу между двумя показаниями.
 * Если телефон перезагружался (счётчик начался с нуля), то все показание —
 * это шаги, сделанные уже после загрузки.
 */
fun counterDelta(previous: Int?, current: Int, rebooted: Boolean): Int = when {
    previous == null -> 0 // Never import steps taken before tracking began.
    rebooted || current < previous -> current.coerceAtLeast(0)
    else -> current - previous
}

/**
 * Перезагрузка определяется по SystemClock.elapsedRealtime(): время с загрузки
 * только растёт, поэтому если оно стало меньше сохранённого — телефон перезагружался.
 * В отличие от «времени загрузки по часам» это не зависит от синхронизации времени.
 */
fun wasRebooted(lastElapsed: Long?, elapsed: Long): Boolean = lastElapsed != null && elapsed < lastElapsed
