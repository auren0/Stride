@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.stride.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.R
import com.example.stride.components.MyText
import com.example.stride.viewmodel.StepsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavesScreen(stepsViewModel: StepsViewModel) {
    val state by stepsViewModel.state.collectAsState()
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    val haptic = LocalHapticFeedback.current

    val month = remember(monthText) { YearMonth.parse(monthText) }
    val today = state.today
    val currentMonth = remember(today) { YearMonth.from(today) }
    val locale = remember { Locale.forLanguageTag("ru") }

    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("LLLL yyyy", locale) }
    val fullDateFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale) }

    // Cached monthly calculations
    val monthlyHistory = remember(state.history, month) {
        state.history.filterKeys { YearMonth.from(it) == month }
    }
    val monthlySteps = remember(monthlyHistory) { monthlyHistory.values.sum() }
    val activeDays = remember(monthlyHistory) { monthlyHistory.values.count { it > 0 } }
    val monthlyDistance = remember(monthlySteps) { monthlySteps * 0.0008 }
    val monthlyCalories = remember(monthlySteps) { (monthlySteps * 0.04).toInt() }

    // Smooth counter animations for monthly stats
    val animatedMonthlySteps by animateIntAsState(
        targetValue = monthlySteps,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "animatedMonthlySteps"
    )
    val animatedMonthlyCalories by animateIntAsState(
        targetValue = monthlyCalories,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "animatedMonthlyCalories"
    )
    val animatedMonthlyDistance by animateFloatAsState(
        targetValue = monthlyDistance.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "animatedMonthlyDistance"
    )

    val offset = remember(month) { month.atDay(1).dayOfWeek.value - 1 }
    val daysInMonth = remember(month) { month.lengthOfMonth() }
    val rows = remember(offset, daysInMonth) { (offset + daysInMonth + 6) / 7 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Expressive Month Navigation Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            monthText = month.minusMonths(1).toString()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.semantics { contentDescription = "Предыдущий месяц" },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedContent(
                            targetState = month,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250))) togetherWith
                                    (slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(200)))
                                } else {
                                    (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250))) togetherWith
                                    (slideOutHorizontally { width -> width / 3 } + fadeOut(tween(200)))
                                }
                            },
                            label = "MonthTitleAnimation"
                        ) { m ->
                            MyText(
                                text = m.format(monthFormatter).replaceFirstChar { it.titlecase(locale) },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            monthText = month.plusMonths(1).toString()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        enabled = month < currentMonth,
                        modifier = Modifier.semantics { contentDescription = "Следующий месяц" },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_forward),
                            contentDescription = null,
                            tint = if (month < currentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Quick jump to Today button if not in current month or today not selected
                if (month != currentMonth || selectedDate != today) {
                    FilledTonalButton(
                        onClick = {
                            monthText = currentMonth.toString()
                            selectedDate = today
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.step),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        MyText(
                            text = "К сегодняшнему дню",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (!state.loaded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    MyText(
                        text = "Загрузка истории шагов…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Material 3 Expressive Monthly Summary Hero Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            MyText(
                                text = "Всего за месяц",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            MyText(
                                text = String.format(locale, "%,d", animatedMonthlySteps),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.step),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                MyText(
                                    text = "шагов",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Secondary metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonthlyMetricChip(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(R.drawable.star),
                            label = "Активных дней",
                            value = "$activeDays из $daysInMonth"
                        )
                        MonthlyMetricChip(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(R.drawable.speed),
                            label = "Дистанция",
                            value = String.format(locale, "%.1f км", animatedMonthlyDistance)
                        )
                        MonthlyMetricChip(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(R.drawable.local_fire_department),
                            label = "Калории",
                            value = String.format(locale, "%,d ккал", animatedMonthlyCalories)
                        )
                    }
                }
            }

            // Calendar Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Weekday headers row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val weekdays = listOf(
                            "Пн" to false,
                            "Вт" to false,
                            "Ср" to false,
                            "Чт" to false,
                            "Пт" to false,
                            "Сб" to true,
                            "Вс" to true
                        )
                        weekdays.forEach { (name, isWeekend) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MyText(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isWeekend) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Calendar Grid Days with smooth transition on month change
                    AnimatedContent(
                        targetState = month,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 4 } + fadeIn(tween(260))) togetherWith
                                (slideOutHorizontally { width -> -width / 4 } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally { width -> -width / 4 } + fadeIn(tween(260))) togetherWith
                                (slideOutHorizontally { width -> width / 4 } + fadeOut(tween(200)))
                            }
                        },
                        label = "CalendarMonthGridTransition"
                    ) { activeMonth ->
                        val activeDaysInMonth = activeMonth.lengthOfMonth()
                        val activeOffset = activeMonth.atDay(1).dayOfWeek.value - 1
                        val activeRows = (activeOffset + activeDaysInMonth + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until activeRows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 7) {
                                        val day = row * 7 + col - activeOffset + 1
                                        if (day !in 1..activeDaysInMonth) {
                                            Spacer(modifier = Modifier.weight(1f).height(68.dp))
                                        } else {
                                            val date = activeMonth.atDay(day)
                                            val steps = state.history[date]
                                            val isFuture = date > today
                                            val isToday = date == today
                                            val isSelected = selectedDate == date
                                            val goalReached = steps != null && steps >= state.goal

                                            ExpressiveDayCell(
                                                modifier = Modifier.weight(1f),
                                                day = day,
                                                steps = steps,
                                                isToday = isToday,
                                                isSelected = isSelected,
                                                isFuture = isFuture,
                                                goalReached = goalReached,
                                                locale = locale,
                                                onClick = {
                                                    if (!isFuture) {
                                                        selectedDate = date
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expressive Legend
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendBadge(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    label = "Выбранный день"
                )
                LegendBadge(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    label = "Сегодня"
                )
                LegendBadge(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    label = "Цель достигнута"
                )
                LegendBadge(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    label = "Есть данные"
                )
            }
        }
    }

    // Material 3 Expressive Modal Bottom Sheet for Day Details
    selectedDate?.let { date ->
        val steps = state.history[date]
        val distance = steps?.let { it * 0.0008 } ?: 0.0
        val calories = steps?.let { (it * 0.04).toInt() } ?: 0
        val dailyGoal = state.goal
        val progress = if (steps != null && dailyGoal > 0) (steps.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
        val animatedSheetProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "sheetProgress"
        )
        val isGoalReached = steps != null && steps >= dailyGoal
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { selectedDate = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Date Title
                Column {
                    MyText(
                        text = date.format(fullDateFormatter).replaceFirstChar { it.titlecase(locale) },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (date == today) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            MyText(
                                text = "Сегодня",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (steps == null || steps == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.step),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            MyText(
                                text = "За этот день нет записей о шагах.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Steps Hero Card in Detail Sheet
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isGoalReached) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isGoalReached) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.step),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                MyText(
                                    text = if (isGoalReached) "Цель выполнена! 🎉" else "Шаги за день",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            MyText(
                                text = String.format(locale, "%,d", steps),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold
                            )

                            // Goal Progress
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { animatedSheetProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MyText(
                                        text = "${(animatedSheetProgress * 100).toInt()}% от цели",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    MyText(
                                        text = "Цель: ${String.format(locale, "%,d", dailyGoal)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Distance and Calories Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(R.drawable.speed),
                            label = "Дистанция",
                            value = String.format(locale, "%.2f км", distance),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        DetailMetricCard(
                            modifier = Modifier.weight(1f),
                            icon = painterResource(R.drawable.local_fire_department),
                            label = "Калории",
                            value = String.format(locale, "%,d ккал", calories),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Close Button
                FilledTonalButton(
                    onClick = { selectedDate = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    MyText(
                        text = "Закрыть",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressiveDayCell(
    modifier: Modifier = Modifier,
    day: Int,
    steps: Int?,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    goalReached: Boolean,
    locale: Locale,
    onClick: () -> Unit
) {
    val animatedBgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.secondaryContainer
            goalReached -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
            steps != null && steps > 0 -> MaterialTheme.colorScheme.surfaceContainerHigh
            isFuture -> MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cellBgColor"
    )

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
        goalReached -> MaterialTheme.colorScheme.onTertiaryContainer
        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    val borderWidth = if (isSelected || isToday) 2.dp else 0.dp
    val cellShape = RoundedCornerShape(16.dp)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cellScale"
    )

    Card(
        modifier = modifier
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cellShape)
            .border(borderWidth, borderColor, cellShape)
            .then(if (!isFuture) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = "День $day: ${steps?.let { "$it шагов" } ?: "нет данных"}"
            },
        colors = CardDefaults.cardColors(
            containerColor = animatedBgColor,
            contentColor = contentColor
        ),
        shape = cellShape,
        elevation = if (isSelected || isToday) CardDefaults.cardElevation(defaultElevation = 3.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Day Number
            MyText(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected || goalReached) FontWeight.ExtraBold else FontWeight.Medium,
                color = contentColor
            )

            // Step count formatted compactly
            val stepText = when {
                isFuture -> "—"
                steps == null || steps == 0 -> "—"
                steps >= 10000 -> String.format(locale, "%.1fk", steps / 1000.0)
                else -> steps.toString()
            }

            MyText(
                text = stepText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = if (steps != null && steps > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor.copy(alpha = if (isFuture) 0.4f else 0.9f),
                maxLines = 1
            )

            // Bottom activity / goal indicator
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (goalReached && !isSelected) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                } else if (steps != null && steps > 0 && !isSelected) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyMetricChip(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            MyText(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            MyText(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LegendBadge(
    color: Color,
    contentColor: Color,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            MyText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DetailMetricCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
            MyText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f)
            )
            MyText(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}
