package com.qrtracker.tracko.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ══════════════════════════════════════════════════════════════════════════════
//  Coral Pulse — Shared Design-System Components
// ══════════════════════════════════════════════════════════════════════════════

// ── Gradient Button (The Radiant Action) ─────────────────────────────────────
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(
                if (enabled) SignatureGradient
                else Brush.linearGradient(listOf(OutlineVariant, OutlineVariant))
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    RoundedCornerShape(9999.dp)
                )
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
                if (icon != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ── Secondary Button ─────────────────────────────────────────────────────────
@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OnSurface,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = OnSurface
            )
        }
    }
}

// ── Editorial Text Field (Soft Focus) ────────────────────────────────────────
@Composable
fun EditorialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Editorial uppercase label
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = if (isFocused) CoralPrimaryFixed else OnSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        // Input container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest)
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> ErrorRed.copy(alpha = 0.5f)
                        isFocused -> CoralPrimaryFixed.copy(alpha = 0.4f)
                        else -> OutlineVariant.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = OutlineVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 0.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = OnSurface
                    ),
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (trailingIcon != null) {
                    Spacer(Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

// ── Status Chip (Pill-Shaped) ────────────────────────────────────────────────
@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, showPulse) = when (status.lowercase()) {
        "active"    -> Triple(EmeraldActiveBg, Color(0xFF15803D), true)
        "misplaced" -> Triple(StatusRedChip, StatusRedText, false)
        "completed" -> Triple(SurfaceContainerHighest, OnSurfaceVariant, false)
        else        -> Triple(SurfaceContainerLow, OnSurfaceVariant, false)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading dot
        if (showPulse) {
            val infiniteTransition = rememberInfiniteTransition(label = "chipPulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    tween(800), RepeatMode.Reverse
                ), label = "dotAlpha"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = alpha))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

// ── Tracking Progress Bar ────────────────────────────────────────────────────
@Composable
fun TrackingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(SurfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(9999.dp))
                .background(
                    if (isError) Brush.horizontalGradient(
                        listOf(Color(0xFFF87171), Color(0xFFEF4444))
                    )
                    else SignatureGradient
                )
        )
    }
}

// ── Glass Top Bar ────────────────────────────────────────────────────────────
@Composable
fun GlassTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit = {
        Text(
            text = "QR Tracker",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                brush = HorizontalBrandGradient
            )
        )
    },
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassWhite)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon != null) {
            navigationIcon()
            Spacer(Modifier.width(12.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            title()
        }
        actions()
    }
}

// ── Bottom Nav Bar ───────────────────────────────────────────────────────────
data class NavItem(
    val label: String,
    val icon: ImageVector? = null,
    val route: String,
    val iconContent: (@Composable (isSelected: Boolean) -> Unit)? = null
)

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    fabIcon: ImageVector? = null,
    fabLabel: String = "Scan",
    onFabClick: () -> Unit = {}
) {
    val centerOverlayItem = items.firstOrNull { it.iconContent != null }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Main bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 52.dp, topEnd = 52.dp),
                    ambientColor = Color(0x0F1A1A1A)
                )
                .background(
                    Color(0xFFD6D9DE),
                    RoundedCornerShape(topStart = 52.dp, topEnd = 52.dp)
                )
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (fabIcon != null && index == 1) {
                    // FAB placeholder space
                    Spacer(Modifier.width(64.dp))
                }

                if (item.iconContent != null) {
                    // Reserve center space; render this item as a floating overlay.
                    Spacer(Modifier.width(88.dp))
                    return@forEachIndexed
                }

                val isSelected = item.route == currentRoute
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(item.route) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) GradientStart else Color(0xFF9CA3AF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) GradientStart else Color(0xFF9CA3AF)
                    )
                }
            }
        }

        if (centerOverlayItem != null && fabIcon == null) {
            val isSelected = centerOverlayItem.route == currentRoute
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemClick(centerOverlayItem.route) }
            ) {
                centerOverlayItem.iconContent?.invoke(isSelected)
                Text(
                    text = centerOverlayItem.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = if (isSelected) GradientStart else Color(0xFF9CA3AF),
                    modifier = Modifier.offset(y = (-16).dp)
                )
            }
        }

        // Floating Scan FAB
        if (fabIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SignatureGradient)
                    .border(4.dp, Surface, CircleShape)
                    .clickable { onFabClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fabIcon,
                    contentDescription = fabLabel,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            // Label under FAB
            Text(
                text = fabLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = GradientStart,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 44.dp)
            )
        }
    }
}

@Composable
fun AdminCreateNavIcon(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .offset(y = (-24).dp)
            .size(74.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(SignatureGradient)
            .border(if (isSelected) 7.dp else 6.dp, Color(0xFFF0F2F5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp
            ),
            color = Color.White
        )
    }
}

// ── Editorial Label ──────────────────────────────────────────────────────────
@Composable
fun EditorialLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = OnSurfaceVariant
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        ),
        color = color,
        modifier = modifier
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  Checkpoint Staff — Bottom Nav Components
//  Visually identical to Admin navbar but with Home / Scan / History tabs
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun StaffScanNavIcon(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .offset(y = (-24).dp)
            .size(74.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(SignatureGradient)
            .border(if (isSelected) 7.dp else 6.dp, Color(0xFFF0F2F5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

