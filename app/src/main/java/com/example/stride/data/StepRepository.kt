package com.example.stride.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
        mutableState.value = mutableState.value.copy(
            loaded = true,
            history = history,
            goal = prefs.getInt("goal", 10000).coerceAtLeast(1)
        )
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            val today = LocalDate.now()
            val current = mutableState.value
            if (current.today == today && today in current.history) return@withLock
            val history = current.history.toMutableMap()
            if (today !in history) {
                history[today] = 0
                prefs.edit().putInt("day_$today", 0).apply()
            }
            mutableState.value = current.copy(today = today, history = history)
        }
    }

    /**
     * @param elapsed SystemClock.elapsedRealtime() в момент события — по нему определяется перезагрузка.
     */
    suspend fun record(date: LocalDate, counter: Int?, elapsed: Long, detected: Int = 0) = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            val previous = if (prefs.contains("counter")) prefs.getInt("counter", 0) else null
            val lastElapsed = if (prefs.contains("last_elapsed")) prefs.getLong("last_elapsed", 0L) else null

            val delta = if (counter != null) {
                counterDelta(previous, counter, wasRebooted(lastElapsed, elapsed))
            } else {
                detected
            }

            val total = (mutableState.value.history[date] ?: 0) + delta
            val editor = prefs.edit().putInt("day_$date", total)
            if (counter != null) {
                editor.putInt("counter", counter).putLong("last_elapsed", elapsed)
            }
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
            val validGoal = goal.coerceAtLeast(1)
            prefs.edit().putInt("goal", validGoal).apply()
            mutableState.value = mutableState.value.copy(goal = validGoal)
        }
    }

    suspend fun status(message: String) = withContext(Dispatchers.IO) {
        mutex.withLock { mutableState.value = mutableState.value.copy(status = message) }
    }

    /**
     * Exports all step history and goal to a JSON string.
     */
    suspend fun exportData(): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            load()
            val json = JSONObject()
            json.put("version", 1)
            json.put("exportDate", LocalDate.now().toString())
            json.put("goal", mutableState.value.goal)

            val historyObj = JSONObject()
            // Sort by date for neat export output
            mutableState.value.history.toSortedMap().forEach { (date, steps) ->
                historyObj.put(date.toString(), steps)
            }
            json.put("history", historyObj)
            json.toString(2)
        }
    }

    /**
     * Imports step history from a JSON string, merging with existing data.
     * Returns the count of imported/updated days.
     */
    suspend fun importData(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                load()
                val json = JSONObject(jsonString)
                val newGoal = json.optInt("goal", mutableState.value.goal).coerceAtLeast(1)
                val historyObj = json.getJSONObject("history")
                val importedHistory = mutableMapOf<LocalDate, Int>()
                val keys = historyObj.keys()
                while (keys.hasNext()) {
                    val dateKey = keys.next()
                    val steps = historyObj.getInt(dateKey)
                    val date = LocalDate.parse(dateKey)
                    if (steps >= 0) {
                        importedHistory[date] = steps
                    }
                }

                if (importedHistory.isEmpty()) {
                    error("В файле нет данных о шагах")
                }

                val merged = mutableState.value.history.toMutableMap()
                importedHistory.forEach { (date, steps) ->
                    merged[date] = maxOf(merged[date] ?: 0, steps)
                }

                val editor = prefs.edit()
                editor.putInt("goal", newGoal)
                merged.forEach { (date, steps) ->
                    editor.putInt("day_$date", steps)
                }
                editor.apply()

                mutableState.value = mutableState.value.copy(
                    history = merged,
                    goal = newGoal
                )
                importedHistory.size
            }
        }
    }

    companion object {
        @Volatile private var instance: StepRepository? = null
        fun get(context: Context): StepRepository = instance ?: synchronized(this) {
            instance ?: StepRepository(context).also { instance = it }
        }
    }
}
