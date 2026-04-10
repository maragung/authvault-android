package auth.vault.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import auth.vault.presentation.viewmodel.SettingsViewModel
import auth.vault.presentation.viewmodel.SettingsUiState
import auth.vault.ui.theme.Orange500
import auth.vault.ui.util.collectAsStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWrapper()

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.primary
            ) {
                TopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SettingSectionTitle("Security") }

            item {
                ExpandableTimeoutCard(
                    currentMinutes = uiState.timeoutMinutes,
                    onTimeoutChange = { viewModel.updateTimeoutMinutes(it) }
                )
            }

            item {
                ExpandableTimeOffsetCard(
                    currentOffset = uiState.timeOffsetSeconds,
                    onOffsetChange = { viewModel.updateTimeOffsetSeconds(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Unlock",
                    subtitle = "Use fingerprint or face ID",
                    checked = uiState.biometricEnabled,
                    onCheckedChange = { viewModel.updateBiometricEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.Security,
                    title = "Hide Token Content",
                    subtitle = "Mask codes by default",
                    checked = uiState.hideTokenContent,
                    onCheckedChange = { viewModel.updateHideTokenContent(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.Lock,
                    title = "Auto-Lock on Background",
                    subtitle = "Lock vault when app goes to background",
                    checked = uiState.autoLockBackground,
                    onCheckedChange = { viewModel.updateAutoLockBackground(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.ScreenLockPortrait,
                    title = "Prevent Screenshots",
                    subtitle = "Block screen capture in vault",
                    checked = uiState.screenshotPrevention,
                    onCheckedChange = { viewModel.updateScreenshotPrevention(it) }
                )
            }

            item { SettingSectionTitle("Appearance") }

            item {
                ThemeSettingCard(
                    currentTheme = uiState.themeMode,
                    currentSchedule = uiState.themeSchedule,
                    onThemeChange = { viewModel.updateThemeMode(it) },
                    onScheduleChange = { viewModel.updateThemeSchedule(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on interactions",
                    checked = uiState.hapticEnabled,
                    onCheckedChange = { viewModel.updateHapticEnabled(it) }
                )
            }

            item {
                ToggleSettingCard(
                    icon = Icons.Default.History,
                    title = "Show Previous Codes",
                    subtitle = "Display previous TOTP codes",
                    checked = uiState.showPreviousCodes,
                    onCheckedChange = { viewModel.updateShowPreviousCodes(it) }
                )
            }

            item {
                SortOrderCard(
                    currentOrder = uiState.sortOrder,
                    onOrderChange = { viewModel.updateSortOrder(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(
                    text = "AuthVault v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            item { Spacer(modifier = Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun ExpandableTimeoutCard(
    currentMinutes: Int,
    onTimeoutChange: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Lock Timeout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("$currentMinutes minutes of inactivity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Icon(if (isExpanded) Icons.Default.Remove else Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(200)) + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { if (currentMinutes > 1) onTimeoutChange(currentMinutes - 5) }, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).size(40.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$currentMinutes min", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Orange500)
                        IconButton(onClick = { if (currentMinutes < 120) onTimeoutChange(currentMinutes + 5) }, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).size(40.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    androidx.compose.material3.Slider(
                        value = currentMinutes.toFloat(),
                        onValueChange = { onTimeoutChange(it.toInt()) },
                        valueRange = 1f..120f,
                        steps = 23,
                        modifier = Modifier.padding(top = 16.dp),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = Orange500,
                            activeTrackColor = Orange500,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableTimeOffsetCard(
    currentOffset: Int,
    onOffsetChange: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Time Offset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${if (currentOffset >= 0) "+" else ""}$currentOffset seconds from device time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Icon(if (isExpanded) Icons.Default.Remove else Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(200)) + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { onOffsetChange(currentOffset - 1) }, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).size(40.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("${if (currentOffset >= 0) "+" else ""}$currentOffset s", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Orange500)
                        IconButton(onClick = { onOffsetChange(currentOffset + 1) }, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).size(40.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf(-30, -10, 0, 10, 30).forEach { offset ->
                            FilterChip(
                                selected = currentOffset == offset,
                                onClick = { onOffsetChange(offset) },
                                label = { Text(if (offset == 0) "0" else "${if (offset > 0) "+" else ""}$offset") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = Orange500,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ThemeSettingCard(
    currentTheme: String,
    currentSchedule: String,
    onThemeChange: (String) -> Unit,
    onScheduleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Choose your preferred appearance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mapOf("system" to Icons.Default.Sync, "light" to Icons.Default.LightMode, "dark" to Icons.Default.DarkMode).forEach { (theme, icon) ->
                    FilterChip(
                        selected = currentTheme == theme,
                        onClick = { onThemeChange(theme) },
                        label = { Text(theme.replaceFirstChar { it.uppercase() }) },
                        leadingIcon = { Icon(icon, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "Auto", "always_on" to "Always On", "always_off" to "Always Off").forEach { (schedule, label) ->
                    FilterChip(
                        selected = currentSchedule == schedule,
                        onClick = { onScheduleChange(schedule) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOrderCard(
    currentOrder: String,
    onOrderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sort, contentDescription = null, tint = Orange500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sort Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("How tokens are displayed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("last_accessed" to "Recent", "name" to "Name", "pinned" to "Pinned").forEach { (value, label) ->
                    FilterChip(
                        selected = currentOrder == value,
                        onClick = { onOrderChange(value) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}
