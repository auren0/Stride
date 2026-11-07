package com.example.stride.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stride.data.StepRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StepsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StepRepository.get(application)
    val state = repository.state

    private val prefs = application.getSharedPreferences("stride_ui_prefs", Context.MODE_PRIVATE)
    private val _isClassicLayout = MutableStateFlow(prefs.getBoolean("is_classic_layout", false))
    val isClassicLayout: StateFlow<Boolean> = _isClassicLayout.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                repository.refresh()
                delay(1000)
            }
        }
    }

    fun toggleStepsLayout() {
        val next = !_isClassicLayout.value
        _isClassicLayout.value = next
        prefs.edit().putBoolean("is_classic_layout", next).apply()
    }

    fun setDailyGoal(goal: Int) { viewModelScope.launch { repository.setGoal(goal) } }
}
