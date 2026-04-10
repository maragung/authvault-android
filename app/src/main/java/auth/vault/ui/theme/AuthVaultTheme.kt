package auth.vault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange900,
    secondary = Orange700,
    onSecondary = Color.White,
    secondaryContainer = Orange100,
    onSecondaryContainer = Orange900,
    tertiary = Orange500,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = Color.Black,
    surface = LightSurface,
    onSurface = Color.Black,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color.Black.copy(alpha = 0.6f),
    outline = NeutralGray.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange500,
    onPrimary = Color.Black,
    primaryContainer = Orange900,
    onPrimaryContainer = Orange100,
    secondary = Orange700,
    onSecondary = Color.Black,
    secondaryContainer = Orange900,
    onSecondaryContainer = Orange100,
    tertiary = Orange500,
    onTertiary = Color.Black,
    error = ErrorRed,
    onError = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = NeutralGray.copy(alpha = 0.5f)
)

@Composable
fun AuthVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
