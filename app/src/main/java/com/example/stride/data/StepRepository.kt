package com.example.stride.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class StepState(
    val loaded: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val history: Map<LocalDate, Int> = emptyMap(),
    val goal: Int = 10000,
    val status: String = "Подключение датчика…"
) {
    val steps: Int get() = history[today] ?: 0
}

class StepRepository private constructor(context: Context) {
    private val app = context.applicationContext
    private val prefs by lazy { app.getSharedPreferences("step_prefs", Context.MODE_PRIVATE) }
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(StepState())
    val state = mutableState.asStateFlow()

    private fun load() {
        if (mutableState.value.loaded) return
        if (!prefs.getBoolean("history_v2", false)) {
            val migrated = mutableMapOf<LocalDate, Int>()
            prefs.getStringSet("steps_history", emptySet()).orEmpty().forEach { entry ->
                val date = legacyDate(entry.substringBefore(":"))
                val steps = entry.substringAfter(":", "").toIntOrNull()
                if (date != null && steps != null) migrated[date] = steps.coerceAtLeast(0)
            }
            legacyDate(prefs.getString("last_date", "").orEmpty())?.let { date ->
                migrated[date] = maxOf(migrated[date] ?: 0, prefs.getInt("daily_steps", 0))
            }
            prefs.edit().apply {
                migrated.forEach { (date, steps) -> putInt("day_$date", steps) }
                putBoolean("history_v2", true)
            }.apply()
        }
        val history = prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith("day_") || value !is Int) null
            else runCatching { LocalDate.parse(key.removePrefix("day_")) to value }.getOrNull()
        }.toMap()
        mutableState.value = mutableState.value.copy(loaded = true, history = history,
            goal = prefs.getInt("goal", 10000).coerceAtLeast(1))
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            val today = LocalDate.now()
            // Missing historical dates stay missing, rather than pretending tracking was active.
            val history = mutableState.value.history.toMutableMap()
            if (today !in history) {
                history[today] = 0
                prefs.edit().putInt("day_$today", 0).apply()
            }
            mutableState.value = mutableState.value.copy(today = today, history = history)
        }
    }

    suspend fun record(date: LocalDate, counter: Int?, boot: Int, detected: Int = 0) = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            val previous = if (prefs.contains("counter")) prefs.getInt("counter", 0) else null
            val delta = if (counter != null) counterDelta(previous, counter, prefs.getInt("boot", -1) == boot) else detected
            val total = (mutableState.value.history[date] ?: 0) + delta
            val editor = prefs.edit().putInt("day_$date", total)
            if (counter != null) editor.putInt("counter", counter).putInt("boot", boot)
            editor.apply()
            val currentStatus = if (mutableState.value.status.contains("Подключение датчика", ignoreCase = true)) "" else mutableState.value.status
            mutableState.value = mutableState.value.copy(
                today = LocalDate.now(),
                history = mutableState.value.history + (date to total),
                status = currentStatus
            )
        }
    }

    suspend fun setGoal(goal: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            prefs.edit().putInt("goal", goal.coerceAtLeast(1)).apply()
            mutableState.value = mutableState.value.copy(goal = goal.coerceAtLeast(1))
        }
    }

    suspend fun status(message: String) = withContext(Dispatchers.IO) {
        mutex.withLock { mutableState.value = mutableState.value.copy(status = message) }
    }

    companion object {
        @Volatile private var instance: StepRepository? = null
        fun get(context: Context): StepRepository = instance ?: synchronized(this) {
            instance ?: StepRepository(context).also { instance = it }
        }
    }
}
