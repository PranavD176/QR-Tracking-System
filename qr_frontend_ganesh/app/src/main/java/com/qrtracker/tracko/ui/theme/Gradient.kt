package com.qrtracker.tracko.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
//  Coral Pulse — Gradients & Elevation
// ══════════════════════════════════════════════════════════════════════════════

/** The Signature Pulse — coral-to-peach 135° gradient for hero moments & CTAs. */
val SignatureGradient = Brush.linearGradient(
    colors = listOf(GradientStart, GradientEnd),
    start  = Offset(0f, 0f),
    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/** Horizontal variant for text gradient effects. */
val HorizontalBrandGradient = Brush.horizontalGradient(
    colors = listOf(GradientStart, GradientEnd)
)

/** Success gradient for verified/valid states. */
val SuccessGradient = Brush.linearGradient(
    colors = listOf(SuccessGradientStart, SuccessGradientEnd),
    start  = Offset(0f, 0f),
    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/** Modifier: Signature gradient background. */
fun Modifier.signatureGradientBackground(): Modifier =
    this.background(SignatureGradient)

/** Modifier: Editorial ambient shadow — diffused tinted shadow for floating elements. */
fun Modifier.editorialShadow(): Modifier =
    this.shadow(
        elevation    = 12.dp,
        ambientColor = Color(0x0F1A1A1A),
        spotColor    = Color(0x0F1A1A1A)
    )

/** Glass surface colors — for frosted-glass overlays. */
val GlassWhite = SurfaceContainerLowest.copy(alpha = 0.8f)
val GlassBlack = Color.Black.copy(alpha = 0.3f)

