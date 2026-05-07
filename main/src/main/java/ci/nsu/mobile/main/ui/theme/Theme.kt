package ci.nsu.mobile.main.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SimplePink = Color(0xFFF48FB1) // Классический нежно-розовый

private val LightColorScheme = lightColorScheme(
    primary = SimplePink,
    onPrimary = Color.White,
    background = Color(0xFFFFF8FA) // Очень легкий розовый оттенок фона
)

@Composable
fun DraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}