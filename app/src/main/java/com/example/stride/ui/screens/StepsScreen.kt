package com.example.stride.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by stepsViewModel.state.collectAsState()
    val isClassicLayout by stepsViewModel.isClassicLayout.collectAsState()

    val dailyGoal = state.goal
    val currentSteps = state.steps
    val caloriesBurned = (currentSteps * 0.04).toInt()
    val distance = currentSteps * 0.0008
    val progress = if (dailyGoal > 0) (currentSteps.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f

    // Smooth animations for counters and progress
    val animatedSteps by animateIntAsState(
        targetValue = currentSteps,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animatedSteps"
    )
    val animatedCalories by animateIntAsState(
        targetValue = caloriesBurned,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animatedCalories"
    )
    val animatedDistance by animateFloatAsState(
        targetValue = distance.toFloat(),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "animatedDistance"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedProgress"
    )

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
            // Elegant loading indicator when connecting sensor instead of a static text banner
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

            // Other status messages (e.g., permissions or errors)
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

            // Permission buttons if needed
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

            // Layout switch between Classic (старый) and Modern (новый) with scale and fade animation
            AnimatedContent(
                targetState = isClassicLayout,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))) togetherWith
                    (fadeOut(tween(200)) + scaleOut(targetScale = 1.05f, animationSpec = tween(200)))
                },
                label = "stepsLayoutTransition"
            ) { classic ->
                if (classic) {
                    ClassicStepsContent(
                        calories = animatedCalories,
                        distance = animatedDistance.toDouble(),
                        progress = animatedProgress,
                        currentSteps = animatedSteps,
                        dailyGoal = dailyGoal
                    )
                } else {
                    ModernStepsContent(
                        calories = animatedCalories,
                        distance = animatedDistance.toDouble(),
                        progress = animatedProgress,
                        currentSteps = animatedSteps,
                        dailyGoal = dailyGoal
                    )
                }
            }
        }
    }
}

/**
 * Классический (оригинальный) экран шагов, который понравился пользователю
 */
@Composable
private fun ClassicStepsContent(
    calories: Int,
    distance: Double,
    progress: Float,
    currentSteps: Int,
    dailyGoal: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Статистика сверху в классическом стиле
        StatsRowClassic(
            calories = calories,
            distance = distance,
            modifier = Modifier.fillMaxWidth()
        )

        // Круг прогресса с шагами (без внешней карточки)
        CircularProgressWithSteps(
            progress = progress,
            currentSteps = currentSteps,
            size = 270.dp,
            strokeWidth = 20.dp
        )

        // Прогресс текст
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
                text = "${(progress * 100).toInt()}% выполнено",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Современный (Material 3 Expressive) экран шагов
 */
@Composable
private fun ModernStepsContent(
    calories: Int,
    distance: Double,
    progress: Float,
    currentSteps: Int,
    dailyGoal: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статистика сверху в карточках Material 3
        StatsRowModern(
            calories = calories,
            distance = distance,
            modifier = Modifier.fillMaxWidth()
        )

        // Круг прогресса внутри выразительной карточки M3
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressWithSteps(
                    progress = progress,
                    currentSteps = currentSteps,
                    size = 220.dp,
                    strokeWidth = 18.dp
                )
            }
        }

        // Прогресс текст
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MyText(
                text = "Цель: ${String.format(Locale.getDefault(), "%,d", dailyGoal)} шагов",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MyText(
                text = "${(progress * 100).toInt()}% выполнено",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatsRowClassic(
    calories: Int,
    distance: Double,
    modifier: Modifier = Modifier
) {
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
private fun StatsRowModern(
    calories: Int,
    distance: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCardModern(
            iconPainter = painterResource(R.drawable.local_fire_department),
            value = "$calories",
            unit = "ккал",
            label = "Сожжено",
            modifier = Modifier.weight(1f)
        )

        StatCardModern(
            iconPainter = painterResource(R.drawable.speed),
            value = "%.1f".format(Locale.getDefault(), distance),
            unit = "км",
            label = "Дистанция",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCardModern(
    iconPainter: Painter,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )

            MyText(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            MyText(
                text = "$unit • $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CircularProgressWithSteps(
    progress: Float,
    currentSteps: Int,
    size: Dp,
    strokeWidth: Dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        // Material 3 Progress Indicator with rounded track
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )

        // Center text - steps
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            MyText(
                text = String.format(Locale.getDefault(), "%,d", currentSteps),
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
