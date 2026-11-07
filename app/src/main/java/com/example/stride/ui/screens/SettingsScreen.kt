package com.example.stride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stride.R
import com.example.stride.components.MyText
import com.example.stride.ui.theme.StrideTheme
import com.example.stride.ui.theme.ThemeMode
import com.example.stride.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val themeViewModel: ThemeViewModel = viewModel()
    val themeState by themeViewModel.themeState

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    MyText(
                        text = "Настройки",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Appearance section
                SettingsSection(title = "Внешний вид") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            ThemeSettingItem(
                                currentTheme = themeState.themeMode,
                                onThemeChanged = { newTheme ->
                                    themeViewModel.setThemeMode(newTheme)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))

                            SettingItem(
                                icon = painterResource(R.drawable.translate),
                                title = "Язык",
                                subtitle = "Русский",
                                onClick = { /* Open language selection */ }
                            )
                        }
                    }
                }

                // Notifications section
                SettingsSection(title = "Уведомления") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        var notificationsEnabled by remember { mutableStateOf(true) }
                        var dailyReminder by remember { mutableStateOf(false) }
                        var achievementNotifications by remember { mutableStateOf(true) }

                        Column {
                            SettingItemWithSwitch(
                                icon = painterResource(R.drawable.notifications),
                                title = "Уведомления",
                                isChecked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )

                            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))

                            SettingItemWithSwitch(
                                icon = painterResource(R.drawable.star),
                                title = "Ежедневное напоминание",
                                isChecked = dailyReminder,
                                onCheckedChange = { dailyReminder = it }
                            )

                            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))

                            SettingItemWithSwitch(
                                icon = painterResource(R.drawable.star),
                                title = "Уведомления о достижениях",
                                isChecked = achievementNotifications,
                                onCheckedChange = { achievementNotifications = it }
                            )
                        }
                    }
                }

                // Account section
                SettingsSection(title = "Аккаунт") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingItem(
                                icon = painterResource(R.drawable.person),
                                title = "Профиль",
                                onClick = { /* Open profile */ }
                            )

                            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))

                            SettingItem(
                                icon = painterResource(R.drawable.privacy_tip),
                                title = "Конфиденциальность",
                                onClick = { /* Open privacy settings */ }
                            )
                        }
                    }
                }

                // About section
                SettingsSection(title = "О приложении") {
                    var showVersionDialog by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingItem(
                                icon = painterResource(R.drawable.settings_filled),
                                title = "Версия",
                                subtitle = "1.0.0",
                                onClick = { showVersionDialog = true }
                            )
                        }
                    }

                    if (showVersionDialog) {
                        AlertDialog(
                            onDismissRequest = { showVersionDialog = false },
                            title = {
                                MyText(
                                    text = "Версия приложения",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            text = {
                                MyText(
                                    text = "Stride v1.0.0\n\nПриложение для отслеживания шагов и активности",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showVersionDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    MyText(
                                        text = "OK",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    )
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        MyText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        content()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingItem(
    icon: Painter,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clip(RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            MyText(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                MyText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.arrow_forward),
            contentDescription = "Перейти",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingItemWithSwitch(
    icon: Painter,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clip(RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            MyText(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                MyText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingItem(
    currentTheme: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { showThemeDialog = true })
            .padding(vertical = 16.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clip(RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(if (currentTheme == ThemeMode.DARK) R.drawable.dark_mode else R.drawable.light_mode),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            MyText(
                text = "Тема",
                style = MaterialTheme.typography.bodyLarge
            )
            MyText(
                text = when (currentTheme) {
                    ThemeMode.LIGHT -> "Светлая"
                    ThemeMode.DARK -> "Темная"
                    ThemeMode.SYSTEM -> "Системная"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            painter = painterResource(R.drawable.arrow_forward),
            contentDescription = "Перейти",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                MyText(
                    text = "Выбор темы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeOption(
                        themeMode = ThemeMode.LIGHT,
                        currentTheme = currentTheme,
                        onSelect = { onThemeChanged(ThemeMode.LIGHT) }
                    )
                    ThemeOption(
                        themeMode = ThemeMode.DARK,
                        currentTheme = currentTheme,
                        onSelect = { onThemeChanged(ThemeMode.DARK) }
                    )
                    ThemeOption(
                        themeMode = ThemeMode.SYSTEM,
                        currentTheme = currentTheme,
                        onSelect = { onThemeChanged(ThemeMode.SYSTEM) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showThemeDialog = false }) {
                    MyText(
                        text = "Готово",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun ThemeOption(
    themeMode: ThemeMode,
    currentTheme: ThemeMode,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        RadioButton(
            selected = currentTheme == themeMode,
            onClick = onSelect
        )

        Spacer(modifier = Modifier.width(16.dp))

        MyText(
            text = when (themeMode) {
                ThemeMode.LIGHT -> "Светлая"
                ThemeMode.DARK -> "Темная"
                ThemeMode.SYSTEM -> "Системная"
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}