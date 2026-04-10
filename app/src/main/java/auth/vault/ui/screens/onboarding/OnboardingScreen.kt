package auth.vault.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import auth.vault.ui.theme.Orange500
import kotlinx.coroutines.launch

data class OnboardingPage(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val description: String)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(Icons.Default.Shield, "Welcome to AuthVault", "Your secure 2FA authentication vault. Store and manage all your one-time passwords safely."),
        OnboardingPage(Icons.Default.Lock, "Military-Grade Encryption", "All your data is encrypted with SQLCipher and AES-256. Only you can access your tokens."),
        OnboardingPage(Icons.Default.QrCode, "Easy Token Management", "Scan QR codes or import from other authenticator apps. Copy codes with a single tap."),
        OnboardingPage(Icons.Default.Fingerprint, "Biometric & PIN Unlock", "Unlock your vault with fingerprint, face ID, PIN, or master password. Auto-lock when you leave."),
        OnboardingPage(Icons.Default.Security, "Full Offline Privacy", "No cloud, no tracking, no analytics. Your data stays on your device only.")
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(pages[page].icon, contentDescription = null, tint = Orange500, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(pages[page].title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(pages[page].description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (i == pagerState.currentPage) Orange500 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    if (i < pages.size - 1) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onComplete) { Text("Skip") }
                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = tween(300)) } },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                    ) { Text("Next") }
                } else {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.height(52.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                    ) { Text("Get Started", style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
    }
}
