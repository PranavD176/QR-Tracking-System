package com.qrtracker.tracko.ui.scan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

@Composable
fun ScanResultScreen(
    navController : NavController,
    result        : String,
    packageDesc   : String,
    ownerName     : String,
    alertSent     : Boolean
) {
    // ── Block back navigation — result screen is not re-entrant ─────────────
    BackHandler { /* consumed — user must use buttons below */ }

    val isValid = result == "valid"
    val isError = result in listOf("misplaced", "rejected", "already_delivered")

    // ── Icon pulse animation ─────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.08f,
        animationSpec  = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    // ── Entry animation ──────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = Surface,
        topBar = {
            GlassTopBar(
                actions = {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = OnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.PACKAGE_LIST),
                    NavItem("Scan", Icons.Default.QrCodeScanner, Routes.SCANNER),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.PACKAGE_LIST),
                    NavItem("Alerts", Icons.Default.Notifications, Routes.ALERTS),
                ),
                currentRoute = Routes.SCANNER,
                onItemClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec  = tween(400)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // ══════════════════════════════════════════════════════════════
                //  Success / Error Status Card
                // ══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isValid) SuccessGradient
                            else androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(MisplacedRedBg, Color(0xFFFDD))
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Pulsing icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isValid) Icons.Outlined.CheckCircle
                                    else if (result == "rejected") Icons.Default.Block
                                    else if (result == "already_delivered") Icons.Default.CheckCircle
                                    else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isValid) EmeraldActive else StatusRedText,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = when (result) {
                                "valid" -> "Package Verified"
                                "rejected" -> "Package Rejected"
                                "already_delivered" -> "Already Delivered"
                                "duplicate" -> "Duplicate Scan"
                                else -> "Misplaced Package"
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isValid) ValidGreen else StatusRedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = when (result) {
                                "valid" -> "Security validation successful"
                                "rejected" -> "This package has been rejected by the receiver"
                                "already_delivered" -> "This package has already been delivered"
                                "duplicate" -> "This scan was already recorded"
                                else -> "Alert sent to sender & receiver"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isValid) ValidGreen.copy(alpha = 0.7f) else StatusRedText.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ══════════════════════════════════════════════════════════════
                //  Package Info Card
                // ══════════════════════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerLowest)
                        .padding(24.dp)
                ) {
                    // Tracking number row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            EditorialLabel(text = "Package")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = packageDesc,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = OnSurface
                            )
                        }
                        StatusChip(
                            status = when (result) {
                                "valid" -> "in_transit"
                                "rejected" -> "rejected"
                                "already_delivered" -> "delivered"
                                else -> "misplaced"
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    // Tonal divider (no-line → background shift)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SurfaceContainerHigh)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Sender row
                    ResultInfoRow(label = "Sender", value = ownerName)
                    Spacer(Modifier.height(10.dp))
                    ResultInfoRow(
                        label = "Status",
                        value = when (result) {
                            "valid" -> "✓ Valid"
                            "rejected" -> "✕ Rejected"
                            "already_delivered" -> "✓ Already Delivered"
                            "duplicate" -> "↻ Duplicate"
                            else -> "⚠ Misplaced"
                        }
                    )
                    if (!isValid && alertSent) {
                        Spacer(Modifier.height(10.dp))
                        ResultInfoRow(label = "Alert", value = "Sent to sender & receiver")
                    }
                    Spacer(Modifier.height(12.dp))

                    // QR thumbnail row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            EditorialLabel(text = "Last Scan")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Just now",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = OnSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ══════════════════════════════════════════════════════════════
                //  Action Buttons
                // ══════════════════════════════════════════════════════════════
                GradientButton(
                    text = "Scan Another",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = {
                        navController.navigate(Routes.SCANNER) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                SecondaryActionButton(
                    text = "Go Home",
                    icon = Icons.Default.Home,
                    onClick = {
                        navController.navigate(Routes.PACKAGE_LIST) {
                            popUpTo(Routes.PACKAGE_LIST) { inclusive = true }
                        }
                    }
                )

                Spacer(Modifier.height(100.dp)) // Bottom nav clearance
            }
        }
    }
}

// ── Info Row ──────────────────────────────────────────────────────────────────
@Composable
private fun ResultInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = OnSurface
        )
    }
}
