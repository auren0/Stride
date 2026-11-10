package com.example.stride

import com.example.stride.accelerometer.StepDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

class StepDetectorTest {
    private val rateHz = 50
    private val dtNs = 1_000_000_000L / rateHz

    private fun run(seconds: Int, signal: (Double) -> Float): Int {
        val detector = StepDetector()
        var steps = 0
        detector.addStepListener { steps = it }
        for (i in 0 until seconds * rateHz) {
            val t = i.toDouble() / rateHz
            detector.onAcceleration(0f, 0f, signal(t), (i + 1) * dtNs)
        }
        return steps
    }

    @Test fun phoneLyingStillCountsNothing() {
        val rnd = Random(1)
        assertEquals(0, run(60) { 9.81f + (rnd.nextGaussian() * 0.05).toFloat() })
    }

    @Test fun smallVibrationCountsNothing() {
        assertEquals(0, run(60) { t -> 9.81f + (0.6 * sin(2 * PI * 25 * t)).toFloat() })
    }

    @Test fun walkingAtTwoStepsPerSecondIsCountedWithoutMultiplying() {
        // 30 seconds of ~2 steps/s => ~60 real steps.
        val steps = run(30) { t -> 9.81f + (3.0 * sin(2 * PI * 2.0 * t)).toFloat() }
        assertTrue("steps=$steps", steps in 48..66)
    }

    @Test fun slowWalkingIsCounted() {
        // 1.4 steps/s for 30s => ~42 steps.
        val steps = run(30) { t -> 9.81f + (3.0 * sin(2 * PI * 1.4 * t)).toFloat() }
        assertTrue("steps=$steps", steps in 32..46)
    }
}
