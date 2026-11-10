package com.example.stride.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.stride.MainActivity
import com.example.stride.R
import com.example.stride.accelerometer.StepDetector
import com.example.stride.data.StepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

class StepForegroundService : Service(), SensorEventListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var sensors: SensorManager
    private val repository by lazy { StepRepository.get(this) }
    private var registered = false
    private var accelerometerDetector: StepDetector? = null
    private var lastNotification = 0L
    private var sensorThread: HandlerThread? = null

    override fun onCreate() {
        super.onCreate()
        sensors = getSystemService(SENSOR_SERVICE) as SensorManager
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("steps", "Подсчёт шагов", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(1, notification("Подсчёт шагов включён"))
            if (!registered) {
                // Try hardware step counter or detector first
                val stepSensor = sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                    ?: sensors.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

                if (stepSensor != null) {
                    registered = sensors.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }

                // If hardware step sensor is not available (e.g. in emulator or budget device), fallback to accelerometer
                if (!registered) {
                    val accel = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    if (accel != null) {
                        val detector = StepDetector().apply {
                            addStepListener { _ ->
                                val date = LocalDate.now()
                                scope.launch {
                                    repository.record(date, counter = null, elapsed = SystemClock.elapsedRealtime(), detected = 1)
                                }
                            }
                        }
                        accelerometerDetector = detector
                        // Акселерометр опрашивается 50 раз/с — обрабатываем в отдельном потоке,
                        // чтобы не нагружать главный поток (иначе приложение лагает).
                        val thread = HandlerThread("stride-accel").also { it.start() }
                        sensorThread = thread
                        registered = sensors.registerListener(
                            detector, accel, SensorManager.SENSOR_DELAY_GAME, Handler(thread.looper)
                        )
                        if (!registered) {
                            thread.quitSafely()
                            sensorThread = null
                        }
                    }
                }

                if (!registered) {
                    scope.launch {
                        repository.status("Датчик шагов недоступен на этом устройстве.")
                        stopSelf()
                    }
                    return START_NOT_STICKY
                }

                scope.launch {
                    while (isActive) {
                        repository.refresh()
                        getSystemService(NotificationManager::class.java).notify(
                            1,
                            notification("Сегодня: ${repository.state.value.steps} шагов")
                        )
                        delay(30_000)
                    }
                }
            }

            // Sensor is registered and connected - clear "Подключение датчика…" status
            if (registered) {
                scope.launch { repository.status("") }
            }
        } catch (_: SecurityException) {
            scope.launch {
                repository.status("Нет доступа к физической активности. Разрешите доступ в настройках приложения.")
                stopSelf()
            }
            return START_NOT_STICKY
        } catch (e: Exception) {
            e.printStackTrace()
            scope.launch {
                repository.status("Ошибка запуска службы подсчёта шагов.")
                stopSelf()
            }
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        val date = LocalDate.now()
        val elapsed = SystemClock.elapsedRealtime()
        val counter = if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) event.values[0].toInt() else null
        val detected = if (counter == null) 1 else 0
        scope.launch {
            repository.record(date, counter, elapsed, detected)
            if (repository.state.value.status.contains("Подключение датчика", ignoreCase = true)) {
                repository.status("")
            }
            if (SystemClock.elapsedRealtime() - lastNotification > 5000) {
                lastNotification = SystemClock.elapsedRealtime()
                getSystemService(NotificationManager::class.java).notify(
                    1,
                    notification("Сегодня: ${repository.state.value.steps} шагов")
                )
            }
        }
    }

    private fun notification(text: String): Notification = NotificationCompat.Builder(this, "steps")
        .setSmallIcon(R.drawable.step).setContentTitle("Stride").setContentText(text)
        .setOngoing(true).setOnlyAlertOnce(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensors.unregisterListener(this)
        accelerometerDetector?.let { sensors.unregisterListener(it) }
        sensorThread?.quitSafely()
        sensorThread = null
        scope.cancel()
        super.onDestroy()
    }
}
