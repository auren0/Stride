package com.example.stride.accelerometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

class StepDetector : SensorEventListener {
    private var stepCount = 0
    private var lastAcceleration = 0f
    private val accelerationThreshold = 10f
    private val stepListeners = mutableListOf<(Int) -> Unit>()

    fun addStepListener(listener: (Int) -> Unit) {
        stepListeners.add(listener)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val acceleration = sqrt(x * x + y * y + z * z)
                val delta = acceleration - lastAcceleration
                lastAcceleration = acceleration

                if (delta > accelerationThreshold) {
                    stepCount++
                    stepListeners.forEach { listener -> listener(stepCount) }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {  }

    fun reset() {
        stepCount = 0
    }

    fun getStepCount(): Int = stepCount
}