package com.example.stride.accelerometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

/**
 * Запасной шагомер по акселерометру (используется только если у телефона нет датчика шагов).
 * Убирает гравитацию фильтром, ищет пики с гистерезисом, ограничивает частоту шагов
 * (не чаще 4 в секунду) и засчитывает шаги только после нескольких подряд в ритме ходьбы.
 */
class StepDetector : SensorEventListener {
    private var stepCount = 0
    private val stepListeners = mutableListOf<(Int) -> Unit>()

    private var gravity = 9.8f
    private val alpha = 0.85f

    private var lastStepTimeNs = 0L
    private val minStepIntervalNs = 250_000_000L
    private val maxStepIntervalNs = 2_000_000_000L

    private var isAboveThreshold = false
    private val stepThresholdHigh = 1.6f
    private val stepThresholdLow = 0.2f

    private var consecutiveSteps = 0
    private val requiredConsecutiveSteps = 4

    fun addStepListener(listener: (Int) -> Unit) {
        stepListeners.add(listener)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        onAcceleration(event.values[0], event.values[1], event.values[2], event.timestamp)
    }

    /** Чистая логика без Android-классов: удобно проверять юнит-тестами. */
    fun onAcceleration(x: Float, y: Float, z: Float, timestampNs: Long) {
        val magnitude = sqrt(x * x + y * y + z * z)
        gravity = alpha * gravity + (1 - alpha) * magnitude
        val dynamicAcc = magnitude - gravity

        if (dynamicAcc > stepThresholdHigh && !isAboveThreshold) {
            isAboveThreshold = true
            val sinceLast = timestampNs - lastStepTimeNs

            if (sinceLast in minStepIntervalNs..maxStepIntervalNs) {
                consecutiveSteps++
                if (consecutiveSteps == requiredConsecutiveSteps) {
                    repeat(requiredConsecutiveSteps) {
                        stepCount++
                        stepListeners.forEach { it(stepCount) }
                    }
                    lastStepTimeNs = timestampNs
                } else if (consecutiveSteps > requiredConsecutiveSteps) {
                    stepCount++
                    stepListeners.forEach { it(stepCount) }
                    lastStepTimeNs = timestampNs
                } else {
                    lastStepTimeNs = timestampNs
                }
            } else if (sinceLast > maxStepIntervalNs) {
                consecutiveSteps = 1
                lastStepTimeNs = timestampNs
            }
        } else if (dynamicAcc < stepThresholdLow) {
            isAboveThreshold = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun reset() {
        stepCount = 0
        consecutiveSteps = 0
        lastStepTimeNs = 0L
        isAboveThreshold = false
    }

    fun getStepCount(): Int = stepCount
}
