package com.example.stride

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stride.data.StepRepository
import com.example.stride.navigation.Navigation
import com.example.stride.services.StepForegroundService
import com.example.stride.ui.theme.StrideTheme
import com.example.stride.ui.theme.ThemeViewModel
import com.example.stride.viewmodel.StepsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        startTrackingIfAllowed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeState by themeViewModel.themeState
            val stepsViewModel: StepsViewModel = viewModel()
            StrideTheme(
                themeMode = themeState.themeMode,
                accentColor = themeState.accentColor
            ) {
                Navigation(
                    stepsViewModel = stepsViewModel,
                    themeViewModel = themeViewModel,
                    onRequestPermission = { requestPermissions() }
                )
            }
        }
        if (savedInstanceState == null) requestPermissions()
    }

    private fun requestPermissions() {
        val missing = buildList {
            if (Build.VERSION.SDK_INT >= 29) add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissions.launch(missing.toTypedArray()) else startTrackingIfAllowed()
    }

    override fun onResume() {
        super.onResume()
        startTrackingIfAllowed()
    }

    private fun startTrackingIfAllowed() {
        if (Build.VERSION.SDK_INT >= 29 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch { StepRepository.get(application).status("Разрешите доступ к физической активности для подсчёта шагов.") }
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, StepForegroundService::class.java))
    }
}
