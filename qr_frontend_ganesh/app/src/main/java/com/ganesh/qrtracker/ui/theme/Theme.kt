package com.ganesh.qrtracker.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Navy,
    onPrimary        = White,
    secondary        = Blue,
    onSecondary      = White,
    tertiary         = LightBlue,
    background       = OffWhite,
    onBackground     = DarkGray,
    surface          = White,
    onSurface        = DarkGray,
    surfaceVariant   = LightGray,
    onSurfaceVariant = Charcoal,
    error            = MisplacedRed,
    onError          = White,
)

private val DarkColorScheme = darkColorScheme(
    primary          = LightBlue,
    onPrimary        = Navy,
    secondary        = Blue,
    onSecondary      = White,
    tertiary         = Navy,
    background       = Charcoal,
    onBackground     = OffWhite,
    surface          = DarkGray,
    onSurface        = OffWhite,
    error            = MisplacedRedBg,
    onError          = MisplacedRed,
)

@Composable
fun QRTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,       // keeping false — our brand colors take priority
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}