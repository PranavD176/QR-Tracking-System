package com.qrtracker.tracko.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  Coral Pulse — "The Kinetic Pulse" Design System
// ══════════════════════════════════════════════════════════════════════════════

// ── Signature Gradient Anchors ───────────────────────────────────────────────
val GradientStart       = Color(0xFFF05A28)
val GradientEnd         = Color(0xFFF5A623)

// ── Primary Brand ────────────────────────────────────────────────────────────
val CoralPrimary        = Color(0xFFA93000)
val CoralPrimaryDim     = Color(0xFF942900)
val CoralPrimaryFixed   = Color(0xFFFF784E)
val CoralPrimaryFixedDim= Color(0xFFFB612F)
val PrimaryContainer    = Color(0xFFFF784E)
val OnPrimary           = Color(0xFFFFEFEB)
val OnPrimaryContainer  = Color(0xFF470F00)

// ── Secondary ────────────────────────────────────────────────────────────────
val CoralSecondary      = Color(0xFF7F5200)
val SecondaryContainer  = Color(0xFFFFC87F)
val OnSecondary         = Color(0xFFFFF0E2)
val OnSecondaryContainer= Color(0xFF644000)

// ── Tertiary ─────────────────────────────────────────────────────────────────
val CoralTertiary       = Color(0xFF843D99)
val TertiaryContainer   = Color(0xFFE293F6)

// ── Surface Hierarchy ("No-Line" Rule) ───────────────────────────────────────
val Surface             = Color(0xFFF6F6F6)   // The canvas
val SurfaceContainerLowest  = Color(0xFFFFFFFF)   // Elevated cards
val SurfaceContainerLow     = Color(0xFFF0F1F1)   // Secondary areas
val SurfaceContainer        = Color(0xFFE7E8E8)
val SurfaceContainerHigh    = Color(0xFFE1E3E3)
val SurfaceContainerHighest = Color(0xFFDBDDDD)   // Nav bars, interactive bg
val SurfaceBright           = Color(0xFFF6F6F6)
val SurfaceDim              = Color(0xFFD3D5D5)
val SurfaceVariant          = Color(0xFFDBDDDD)

// ── On Surface / Text ────────────────────────────────────────────────────────
val OnSurface           = Color(0xFF2D2F2F)   // Primary text (never #000000)
val OnSurfaceVariant    = Color(0xFF5A5C5C)   // Secondary text
val OnBackground        = Color(0xFF2D2F2F)

// ── Outline ──────────────────────────────────────────────────────────────────
val Outline             = Color(0xFF767777)
val OutlineVariant      = Color(0xFFACADAD)   // Ghost borders at 20% opacity

// ── Inverse ──────────────────────────────────────────────────────────────────
val InverseSurface      = Color(0xFF0C0F0F)
val InverseOnSurface    = Color(0xFF9C9D9D)
val InversePrimary      = Color(0xFFFB612F)

// ── Error ────────────────────────────────────────────────────────────────────
val ErrorRed            = Color(0xFFB31B25)
val ErrorDim            = Color(0xFF9F0519)
val ErrorContainer      = Color(0xFFFB5151)
val OnError             = Color(0xFFFFEFEE)
val OnErrorContainer    = Color(0xFF570008)

// ── Status (Kinetic Pulse semantics) ─────────────────────────────────────────
val ValidGreen          = Color(0xFF1E5C34)
val ValidGreenBg        = Color(0xFFE8F5EE)
val EmeraldActive       = Color(0xFF10B981)
val EmeraldActiveBg     = Color(0xFFDCFCE7)
val MisplacedRed        = Color(0xFF7B1515)
val MisplacedRedBg      = Color(0xFFFFEBEB)
val StatusRedChip       = Color(0xFFFEE2E2)
val StatusRedText       = Color(0xFFB91C1C)

// ── Success Gradient (Scan result) ───────────────────────────────────────────
val SuccessGradientStart = Color(0xFFE8F5E9)
val SuccessGradientEnd   = Color(0xFFC8E6C9)

// ── Checkpoint Status (Admin operational states) ─────────────────────────────
val ReceivedGreen       = Color(0xFF22C55E)
val ReceivedGreenBg     = Color(0xFFF0FDF4)
val ReceivedGreenBorder = Color(0xFFDCFCE7)
val MisplacedOrange     = Color(0xFFEA580C)
val MisplacedOrangeBg   = Color(0xFFFFF7ED)
val MisplacedOrangeBorder = Color(0xFFFED7AA)
val DuplicateGray       = Color(0xFF78716C)
val DuplicateGrayBg     = Color(0xFFF5F5F4)
val DuplicateGrayBorder = Color(0xFFE7E5E4)

// ── Alert Severity ───────────────────────────────────────────────────────────
val HighSeverityBg      = Color(0xFFFB5151)   // same as ErrorContainer
val HighSeverityText    = Color(0xFF570008)
val MediumSeverityBg    = Color(0xFFFFC87F)   // same as SecondaryContainer
val MediumSeverityText  = Color(0xFF644000)
val LowSeverityBg       = Color(0xFFE1E3E3)
val LowSeverityText     = Color(0xFF5A5C5C)

// ── Legacy (kept for backward-compat, NOT used by new screens) ───────────────
val Navy        = Color(0xFF1F3864)
val Blue        = Color(0xFF2E75B6)
val LightBlue   = Color(0xFFBDD7EE)
val PaleBlue    = Color(0xFFEBF3FB)
val White       = Color(0xFFFFFFFF)
val OffWhite    = Color(0xFFF7F9FC)
val LightGray   = Color(0xFFF2F2F2)
val MidGray     = Color(0xFFCCCCCC)
val DarkGray    = Color(0xFF404040)
val Charcoal    = Color(0xFF2D2D2D)
val Purple80    = Color(0xFFD0BCFF)
val PurpleGrey80= Color(0xFFCCC2DC)
val Pink80      = Color(0xFFEFB8C8)
val Purple40    = Color(0xFF6650A4)
val PurpleGrey40= Color(0xFF625B71)
val Pink40      = Color(0xFF7D5260)
