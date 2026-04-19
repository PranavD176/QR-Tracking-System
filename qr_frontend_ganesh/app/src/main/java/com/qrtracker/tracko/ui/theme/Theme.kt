package com.qrtracker.tracko.ui.theme

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

// ══════════════════════════════════════════════════════════════════════════════
//  Coral Pulse — Material3 Theme
// ══════════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    // Brand
    primary             = CoralPrimary,
    onPrimary           = OnPrimary,
    primaryContainer    = PrimaryContainer,
    onPrimaryContainer  = OnPrimaryContainer,

    // Secondary
    secondary           = CoralSecondary,
    onSecondary         = OnSecondary,
    secondaryContainer  = SecondaryContainer,
    onSecondaryContainer= OnSecondaryContainer,

    // Tertiary
    tertiary            = CoralTertiary,
    tertiaryContainer   = TertiaryContainer,

    // Background & Surface
    background          = Surface,
    onBackground        = OnBackground,
    surface             = Surface,
    onSurface           = OnSurface,
    surfaceVariant      = SurfaceVariant,
    onSurfaceVariant    = OnSurfaceVariant,
    surfaceContainerLowest  = SurfaceContainerLowest,
    surfaceContainerLow     = SurfaceContainerLow,
    surfaceContainer        = SurfaceContainer,
    surfaceContainerHigh    = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceBright           = SurfaceBright,
    surfaceDim              = SurfaceDim,

    // Outline
    outline             = Outline,
    outlineVariant      = OutlineVariant,

    // Inverse
    inverseSurface      = InverseSurface,
    inverseOnSurface    = InverseOnSurface,
    inversePrimary      = InversePrimary,

    // Error
    error               = ErrorRed,
    onError             = OnError,
    errorContainer      = ErrorContainer,
    onErrorContainer    = OnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary             = CoralPrimaryFixed,
    onPrimary           = OnPrimaryContainer,
    secondary           = SecondaryContainer,
    onSecondary         = OnSecondaryContainer,
    background          = InverseSurface,
    onBackground        = InverseOnSurface,
    surface             = Color(0xFF1A1C1C),
    onSurface           = Color(0xFFE1E3E3),
    error               = ErrorContainer,
    onError             = OnErrorContainer,
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
            // Use gradient start color for status bar — matches the editorial hero headers
            window.statusBarColor = GradientStart.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false  // white icons on coral status bar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
