package com.gestureshare.core.ui.theme

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
    primary = Color(0xFF1A6B52),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA5F2D3),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4C6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DB),
    onSecondaryContainer = Color(0xFF082018),
    tertiary = Color(0xFF3E6374),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC2E8FC),
    onTertiaryContainer = Color(0xFF001F2A),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E312F),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = Color(0xFF8AD5B8),
    surfaceDim = Color(0xFFD9DDD9),
    surfaceBright = Color(0xFFFBFDF9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F5F1),
    surfaceContainer = Color(0xFFEDEFEB),
    surfaceContainerHigh = Color(0xFFE7E9E6),
    surfaceContainerHighest = Color(0xFFE2E4E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AD5B8),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFFA5F2D3),
    secondary = Color(0xFFB3CCBF),
    onSecondary = Color(0xFF1E352C),
    secondaryContainer = Color(0xFF354C42),
    onSecondaryContainer = Color(0xFFCEE9DB),
    tertiary = Color(0xFFA6CCE0),
    onTertiary = Color(0xFF093544),
    tertiaryContainer = Color(0xFF264B5C),
    onTertiaryContainer = Color(0xFFC2E8FC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE2E4E0),
    surface = Color(0xFF101412),
    onSurface = Color(0xFFE2E4E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outline = Color(0xFF8A938D),
    outlineVariant = Color(0xFF404944),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E4E0),
    inverseOnSurface = Color(0xFF2E312F),
    inversePrimary = Color(0xFF1A6B52),
    surfaceDim = Color(0xFF101412),
    surfaceBright = Color(0xFF363A38),
    surfaceContainerLowest = Color(0xFF0B0F0D),
    surfaceContainerLow = Color(0xFF191C1A),
    surfaceContainer = Color(0xFF1D201E),
    surfaceContainerHigh = Color(0xFF272B29),
    surfaceContainerHighest = Color(0xFF323634)
)

@Composable
fun GestureShareTheme(
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
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
