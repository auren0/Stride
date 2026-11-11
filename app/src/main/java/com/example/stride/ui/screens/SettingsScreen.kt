package com.example.stride.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stride.R
import com.example.stride.components.MyText
import com.example.stride.ui.theme.AccentColor
import com.example.stride.ui.theme.Purple40
import com.example.stride.ui.theme.Purple80
import com.example.stride.ui.theme.ThemeMode
import com.example.stride.ui.theme.ThemeViewModel
import com.example.stride.viewmodel.StepsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SettingsScreen(
    stepsViewModel: StepsViewModel,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeState by themeViewModel.themeState

    var statusDialogText by remember { mutableStateOf<String?>(null) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = stepsViewModel.exportData()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                    statusDialogText = "Данные о шагах сохранены в выбранный файл."
                } catch (e: Exception) {
                    statusDialogText = "Ошибка при экспорте: ${e.localizedMessage ?: "неизвестная ошибка"}"
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    } ?: ""
                    stepsViewModel.importData(content).fold(
                        onSuccess = { count ->
                            statusDialogText = "Импортированы данные за $count дн.\nИстория шагов обновлена."
                        },
                        onFailure = { error ->
                            statusDialogText = "Не удалось импортировать данные:\n${error.localizedMessage ?: "неверный формат файла"}"
                        }
                    )
                } catch (e: Exception) {
                    statusDialogText = "Ошибка чтения файла: ${e.localizedMessage ?: "неизвестная ошибка"}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsHero()

        SectionTitle("Тема оформления")
        val currentAccent = MaterialTheme.colorScheme.primary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeTile(
                title = "Светлая",
                preview = ThemePreview(Color(0xFFFFFBFE), Color(0xFFE7E0EC), currentAccent),
                selected = themeState.themeMode == ThemeMode.LIGHT,
                onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeTile(
                title = "Тёмная",
                preview = ThemePreview(Color(0xFF1C1B1F), Color(0xFF2B2930), currentAccent),
                selected = themeState.themeMode == ThemeMode.DARK,
                onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeTile(
                title = "OLED",
                preview = ThemePreview(Color(0xFF000000), Color(0xFF1A1A1A), currentAccent),
                selected = themeState.themeMode == ThemeMode.OLED,
                onClick = { themeViewModel.setThemeMode(ThemeMode.OLED) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("Акцентный цвет")
        AccentColorPicker(
            selectedAccent = themeState.accentColor,
            isDark = themeState.themeMode != ThemeMode.LIGHT,
            onSelectAccent = { themeViewModel.setAccentColor(it) }
        )

        Spacer(Modifier.height(8.dp))
        SectionTitle("Данные")
        SettingsGroup {
            item(
                iconRes = R.drawable.ic_upload,
                title = "Экспорт в файл",
                subtitle = "Сохранить историю шагов в JSON",
                iconBackground = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { exportFileLauncher.launch("stride_steps_backup_${LocalDate.now()}.json") }
            )
            item(
                iconRes = R.drawable.ic_share,
                title = "Поделиться данными",
                subtitle = "Отправить через мессенджер или облако",
                iconBackground = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    coroutineScope.launch {
                        try {
                            val json = stepsViewModel.exportData()
                            val send = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(send, "Экспорт данных о шагах"))
                        } catch (e: Exception) {
                            statusDialogText = "Ошибка экспорта: ${e.localizedMessage}"
                        }
                    }
                }
            )
            item(
                iconRes = R.drawable.ic_download,
                title = "Импорт из файла",
                subtitle = "Загрузить историю шагов из JSON",
                iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }
            )
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("О приложении")
        SettingsGroup {
            item(
                iconRes = R.drawable.step,
                title = "Stride 1.2.0",
                subtitle = "Шаги считает датчик телефона без лишнего расхода батареи",
                iconBackground = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = null
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    statusDialogText?.let { text ->
        AlertDialog(
            onDismissRequest = { statusDialogText = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                MyText(
                    text = "Резервное копирование",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { MyText(text = text, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { statusDialogText = null }) {
                    MyText(text = "OK", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun SettingsHero() {
    val container = MaterialTheme.colorScheme.primaryContainer
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(36.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                // Декоративные круги рисуются на этапе draw — без лишних слоёв и теней.
                .drawBehind {
                    drawCircle(accent.copy(alpha = 0.12f), radius = size.height * 0.9f, center = Offset(size.width * 0.95f, 0f))
                    drawCircle(accent.copy(alpha = 0.10f), radius = size.height * 0.45f, center = Offset(size.width * 0.72f, size.height))
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.step),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.width(18.dp))
            Column {
                MyText(
                    text = "Stride",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = onContainer
                )
                MyText(
                    text = "Оформление и данные",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    MyText(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp)
    )
}

private class ThemePreview(val background: Color, val card: Color, val accent: Color)

@Composable
private fun ThemeTile(
    title: String,
    preview: ThemePreview,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "tileBorder"
    )
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(shape)
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .border(BorderStroke(2.5.dp, borderColor), shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Мини-превью темы
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(preview.background)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(preview.accent)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(preview.card)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(preview.card)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            MyText(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private class SettingsEntry(
    val iconRes: Int,
    val title: String,
    val subtitle: String,
    val iconBackground: Color,
    val iconTint: Color,
    val onClick: (() -> Unit)?
)

private class SettingsGroupScope {
    val entries = mutableListOf<SettingsEntry>()
    fun item(
        iconRes: Int,
        title: String,
        subtitle: String,
        iconBackground: Color,
        iconTint: Color,
        onClick: (() -> Unit)?
    ) {
        entries += SettingsEntry(iconRes, title, subtitle, iconBackground, iconTint, onClick)
    }
}

/**
 * Группа в стиле Material 3 Expressive: крупные скругления снаружи группы
 * и маленькие между соседними пунктами.
 */
@Composable
private fun SettingsGroup(content: @Composable SettingsGroupScope.() -> Unit) {
    val scope = SettingsGroupScope()
    scope.content()
    val entries = scope.entries
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        entries.forEachIndexed { index, entry ->
            val big = 28.dp
            val small = 6.dp
            val shape: Shape = RoundedCornerShape(
                topStart = if (index == 0) big else small,
                topEnd = if (index == 0) big else small,
                bottomStart = if (index == entries.lastIndex) big else small,
                bottomEnd = if (index == entries.lastIndex) big else small
            )
            SettingsRow(entry, shape)
        }
    }
}

@Composable
private fun SettingsRow(entry: SettingsEntry, shape: Shape) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(if (entry.onClick != null) Modifier.clickable(onClick = entry.onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(entry.iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(entry.iconRes),
                contentDescription = null,
                tint = entry.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            MyText(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            MyText(
                text = entry.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccentColorPicker(
    selectedAccent: AccentColor,
    isDark: Boolean,
    onSelectAccent: (AccentColor) -> Unit
) {
    val context = LocalContext.current
    val isDynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val systemAccentColor = remember(context, isDark, isDynamicAvailable) {
        if (isDynamicAvailable) {
            if (isDark) dynamicDarkColorScheme(context).primary
            else dynamicLightColorScheme(context).primary
        } else {
            Purple40
        }
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentColor.entries.forEach { accent ->
                val isSelected = accent == selectedAccent
                val color = if (accent == AccentColor.SYSTEM) systemAccentColor else accent.previewColor

                AccentColorItem(
                    accent = accent,
                    color = color,
                    isSelected = isSelected,
                    isDynamic = accent == AccentColor.SYSTEM && isDynamicAvailable,
                    onClick = { onSelectAccent(accent) }
                )
            }
        }
    }
}

@Composable
private fun AccentColorItem(
    accent: AccentColor,
    color: Color,
    isSelected: Boolean,
    isDynamic: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "accentScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), CircleShape)
                    } else {
                        Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (accent == AccentColor.SYSTEM) {
                Icon(
                    painter = painterResource(R.drawable.settings_outlined),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        MyText(
            text = accent.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
