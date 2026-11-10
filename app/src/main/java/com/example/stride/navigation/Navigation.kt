package com.example.stride.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.R
import com.example.stride.components.MyText
import com.example.stride.ui.screens.SavesScreen
import com.example.stride.ui.screens.SettingsScreen
import com.example.stride.ui.screens.StepsScreen
import com.example.stride.ui.theme.ThemeViewModel
import com.example.stride.viewmodel.StepsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    stepsViewModel: StepsViewModel,
    themeViewModel: ThemeViewModel,
    onRequestPermission: () -> Unit
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showGoal by rememberSaveable { mutableStateOf(false) }
    val stateHolder = rememberSaveableStateHolder()
    val titles = listOf("Шаги", "Календарь", "Настройки")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = titles[selected],
                        transitionSpec = {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                        },
                        label = "topBarTitleTransition"
                    ) { titleText ->
                        MyText(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (selected == 0) {
                        FilledTonalIconButton(
                            onClick = { showGoal = true },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.goal),
                                contentDescription = "Изменить цель",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(28.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 3.dp
            ) {
                // Tab 0: Шаги
                NavigationBarItem(
                    selected = selected == 0,
                    onClick = { selected = 0 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.step),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        MyText(
                            text = titles[0],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected == 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    alwaysShowLabel = true
                )

                // Tab 1: Календарь (with proper calendar icon)
                NavigationBarItem(
                    selected = selected == 1,
                    onClick = { selected = 1 },
                    icon = {
                        Icon(
                            painter = painterResource(if (selected == 1) R.drawable.calendar_filled else R.drawable.calendar_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        MyText(
                            text = titles[1],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected == 1) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    alwaysShowLabel = true
                )

                // Tab 2: Настройки
                NavigationBarItem(
                    selected = selected == 2,
                    onClick = { selected = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(if (selected == 2) R.drawable.settings_filled else R.drawable.settings_outlined),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        MyText(
                            text = titles[2],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected == 2) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.surface
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    // Только плавное появление: слайд целого экрана заметно тормозит на старых телефонах.
                    fadeIn(tween(160)) togetherWith fadeOut(tween(100))
                },
                label = "ScreenNavigationTransition"
            ) { page ->
                stateHolder.SaveableStateProvider(page) {
                    when (page) {
                        0 -> StepsScreen(stepsViewModel, onRequestPermission)
                        1 -> SavesScreen(stepsViewModel)
                        2 -> SettingsScreen(stepsViewModel, themeViewModel)
                    }
                }
            }
        }
    }

    if (showGoal) {
        var goal by rememberSaveable { mutableStateOf(stepsViewModel.state.value.goal.toString()) }
        val parsed = goal.toIntOrNull()
        AlertDialog(
            onDismissRequest = { showGoal = false },
            shape = RoundedCornerShape(28.dp),
            title = {
                MyText(
                    text = "Цель по шагам",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it.filter(Char::isDigit).take(7) },
                    label = { MyText(text = "Шагов в день", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = (parsed != null) && (parsed > 0),
                    shape = RoundedCornerShape(20.dp),
                    onClick = {
                        parsed?.let(stepsViewModel::setDailyGoal)
                        showGoal = false
                    }
                ) {
                    MyText(
                        text = "Сохранить",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoal = false },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    MyText(
                        text = "Отмена",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }
            }
        )
    }
}
