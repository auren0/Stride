package com.example.stride.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stride.data.StepRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StepsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StepRepository.get(application)
    val state = repository.state

    init {
        viewModelScope.launch {
            while (isActive) {
                repository.refresh()
                // Нужно только для смены даты в полночь — секундный опрос лишь грузил старый телефон.
                delay(20_000)
            }
        }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { repository.setGoal(goal) }
    }

    suspend fun exportData(): String = repository.exportData()

    suspend fun importData(jsonString: String): Result<Int> = repository.importData(jsonString)
}
