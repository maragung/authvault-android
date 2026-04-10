package auth.vault.ui.screens.pingenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import auth.vault.ui.theme.Orange500
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var length by remember { mutableStateOf(16) }
    var useUppercase by remember { mutableStateOf(true) }
    var useLowercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }

    fun generatePassword(): String {
        val chars = buildString {
            if (useUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (useLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (useNumbers) append("0123456789")
            if (useSymbols) append("!@#$%^&*()_+-=[]{}|;:,.<>?")
            if (isEmpty()) append("abcdefghijklmnopqrstuvwxyz")
        }
        return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.primary) {
                TopAppBar(
                    title = { Text("Password Generator", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Orange500, modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            if (generatedPassword.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Generated Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(generatedPassword, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(12.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { generatedPassword = generatePassword(); haptic.performHapticFeedback(HapticFeedbackType.Confirm) }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange500)) { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 4.dp)); Text("Regenerate") }
                            Button(onClick = { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Password", generatedPassword)); haptic.performHapticFeedback(HapticFeedbackType.Confirm) }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 4.dp)); Text("Copy") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Length: $length", fontWeight = FontWeight.SemiBold)
                    Slider(value = length.toFloat(), onValueChange = { length = it.toInt() }, valueRange = 8f..64f, steps = 55, colors = SliderDefaults.colors(thumbColor = Orange500, activeTrackColor = Orange500))

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("Uppercase (A-Z)" to useUppercase, "Lowercase (a-z)" to useLowercase, "Numbers (0-9)" to useNumbers, "Symbols (!@#)" to useSymbols).forEach { (label, checked) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { when(label) { "Uppercase (A-Z)" -> useUppercase = !useUppercase; "Lowercase (a-z)" -> useLowercase = !useLowercase; "Numbers (0-9)" -> useNumbers = !useNumbers; else -> useSymbols = !useSymbols } }, verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = { when(label) { "Uppercase (A-Z)" -> useUppercase = !useUppercase; "Lowercase (a-z)" -> useLowercase = !useLowercase; "Numbers (0-9)" -> useNumbers = !useNumbers; else -> useSymbols = !useSymbols } })
                            Text(label, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = { generatedPassword = generatePassword(); haptic.performHapticFeedback(HapticFeedbackType.Confirm) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange500)) {
                        Text("Generate Password", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
