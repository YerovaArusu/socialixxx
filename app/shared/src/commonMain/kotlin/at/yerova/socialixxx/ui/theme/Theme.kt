package at.yerova.socialixxx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val XxxlutzRed = Color(0xFFE20015)
val XxxlutzRedDark = Color(0xFFFF5252)

// 2. Das Light Theme
private val LightColors = lightColorScheme(
    primary = XxxlutzRed,
    onPrimary = Color.White,
    secondary = Color(0xFF333333),
    onSecondary = Color.White,
    background = Color(0xFFF5F3F7),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF757575),
    outlineVariant = Color(0xFFE0E0E0),
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFEBEE)
)

private val DarkColors = darkColorScheme(
    primary = XxxlutzRedDark,
    onPrimary = Color.White,
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF1A1A1A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outlineVariant = Color(0xFF333333),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

@Composable
fun SocialixxxTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}