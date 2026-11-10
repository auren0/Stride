package com.example.stride.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.R
import com.example.stride.components.MyText
import com.example.stride.viewmodel.StepsViewModel
import java.util.Locale

@Composable
fun StepsScreen(stepsViewModel: StepsViewModel, onRequestPermission: () -> Unit = {}) {
    val context = LocalContext.current
    val state by stepsViewModel.state.collectAsState()

    val isSensorConnecting = state.status.contains("Подключение датчика", ignoreCase = true)
    val hasOtherStatus = state.status.isNotBlank() && !isSensorConnecting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!state.loaded) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            AnimatedVisibility(
                visible = isSensorConnecting,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        MyText(
                            text = "Подключение датчика…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (hasOtherStatus) {
                MyText(
                    text = state.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                )
            }

            if (state.status.contains("Разрешите", ignoreCase = true)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(onClick = onRequestPermission) {
                        MyText(
                            text = "Разрешить доступ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedButton(onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }) {
                        MyText(
                            text = "Настройки приложения",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            ClassicStepsContent(steps = state.steps, dailyGoal = state.goal)
        }
    }
}

/**
 * Классический (оригинальный) экран шагов.
 *
 * Анимации счётчиков вынесены в отдельные «листовые» composable-функции: так на каждом кадре
 * анимации перерисовывается только маленький кусок экрана, а не весь экран целиком.
 */
@Composable
private fun ClassicStepsContent(steps: Int, dailyGoal: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StatsRowClassic(steps = steps, modifier = Modifier.fillMaxWidth())

        CircularProgressWithSteps(
            steps = steps,
            goal = dailyGoal,
            size = 270.dp,
            strokeWidth = 20.dp
        )

        val percent = if (dailyGoal > 0) ((steps.toFloat() / dailyGoal).coerceIn(0f, 1f) * 100).toInt() else 0
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MyText(
                text = "Цель: ${String.format(Locale.getDefault(), "%,d", dailyGoal)} шагов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MyText(
                text = "$percent% выполнено",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatsRowClassic(steps: Int, modifier: Modifier = Modifier) {
    val calories by animateIntAsState(
        targetValue = (steps * 0.04).toInt(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "calories"
    )
    val distance by animateFloatAsState(
        targetValue = (steps * 0.0008).toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "distance"
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCardClassic(
            iconPainter = painterResource(R.drawable.local_fire_department),
            value = "$calories",
            unit = "ккал",
            label = "Сожжено",
            modifier = Modifier.weight(1f)
        )
        StatCardClassic(
            iconPainter = painterResource(R.drawable.speed),
            value = "%.1f".format(Locale.getDefault(), distance),
            unit = "км",
            label = "Дистанция",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCardClassic(
    iconPainter: Painter,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            MyText(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            MyText(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MyText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CircularProgressWithSteps(
    steps: Int,
    goal: Int,
    size: Dp,
    strokeWidth: Dp
) {
    val targetProgress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    // Прогресс держим как State и читаем только в лямбде кольца — это фаза рисования,
    // без перекомпозиции на каждом кадре.
    val animatedProgress = animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val animatedSteps by animateIntAsState(
        targetValue = steps,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "steps"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress.value },
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MyText(
                text = String.format(Locale.getDefault(), "%,d", animatedSteps),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (size > 240.dp) 52.sp else 46.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            MyText(
                text = "шагов",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
