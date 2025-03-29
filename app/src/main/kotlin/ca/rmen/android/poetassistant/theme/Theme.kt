package ca.rmen.android.poetassistant.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.google.android.material.color.utilities.DynamicColor

val lightColorTheme = lightColorScheme(
    background = LightBackground,
    error = LightError,
    onPrimary = LightOnPrimary,
    onSurface = LightOnSurface,
    primary = LightPrimary,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSecondary = LightOnSecondary,
)

val darkColorTheme = darkColorScheme(
    background = DarkBackground,
    error = DarkError,
    onPrimary = DarkOnPrimary,
    onSurface = DarkOnSurface,
    primary = DarkPrimary,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSecondary = DarkOnSecondary,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) darkColorTheme else lightColorTheme, content = content)
}
