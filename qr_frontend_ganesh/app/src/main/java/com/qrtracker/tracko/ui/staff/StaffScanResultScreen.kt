package com.qrtracker.tracko.ui.staff

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  Staff Scan Result — Handles 3 states: success, misplaced, delivered
//  Pixel-accurate to all three Stitch designs
//  Backend-ready: status driven by route argument
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun StaffScanResultScreen(
    navController: NavController,
    orderId: String,
    status: String,       // "success", "misplaced", "delivered"
    currentCheckpoint: String,
    nextCheckpoint: String
) {
    when (status.lowercase()) {
        "delivered" -> DeliveredState(navController, orderId, currentCheckpoint)
        "misplaced" -> MisplacedState(navController, orderId, currentCheckpoint, nextCheckpoint)
        else -> SuccessState(navController, orderId, currentCheckpoint, nextCheckpoint)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  STATE 1: SUCCESS
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun SuccessState(
    navController: NavController,
    orderId: String,
    currentCheckpoint: String,
    nextCheckpoint: String
) {
    Scaffold(
        containerColor = Surface,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            // ── Glass Top Bar ────────────────────────────────────────────────
            StaffResultTopBar()

            // ── Success Hero ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape)
                            .blur(32.dp)
                    )
                    // Main icon circle
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLowest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Scan Successful",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnSurface
                )


                Spacer(Modifier.height(8.dp))

                Text(
                    "Checkpoint verified for the current route",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // ── Bento Grid Details ───────────────────────────────────────────
            // Order ID Card (full width)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text(
                    "ORDER ID",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        orderId,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface
                    )
                    Icon(Icons.Default.QrCode2, null, tint = CoralPrimary, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Current + Next Checkpoint cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Checkpoint
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .padding(24.dp)
                ) {
                    Text(
                        "CURRENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        currentCheckpoint,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Matched",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF16A34A)
                        )
                    }
                }

                // Next Checkpoint
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        "NEXT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        nextCheckpoint,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AltRoute,
                            null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Scheduled",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = CoralPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Route Progress Visualization ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .padding(32.dp)
            ) {
                // Progress dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(CoralPrimary)
                                .border(4.dp, PrimaryContainer.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            currentCheckpoint.substringBefore("-"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = OnSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceContainerHighest)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.5f)
                                .background(SignatureGradient)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(OnSurfaceVariant.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            nextCheckpoint.substringBefore("-"),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Package info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLowest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, null, tint = CoralPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Express Parcel",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = OnSurface
                        )
                        Text(
                            "Updated 2 mins ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(96.dp)) // Space for bottom button
        }

        // ── Fixed Bottom Action Button ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.4f))
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SignatureGradient)
                    .clickable {
                        navController.navigate(Routes.STAFF_SCAN) {
                            popUpTo(Routes.STAFF_HOME)
                            launchSingleTop = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Next Scan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

}

// ══════════════════════════════════════════════════════════════════════════════
//  STATE 2: MISPLACED
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MisplacedState(
    navController: NavController,
    orderId: String,
    expectedCheckpoint: String,
    actualCheckpoint: String
) {
    Scaffold(containerColor = Surface) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Glass Top Bar ────────────────────────────────────────────
                StaffResultTopBar()

                // ── Error Hero Section ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Red outer border with glow
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, ErrorRed, RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warning icon circle
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                null,
                                tint = ErrorRed,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Misplaced Package Detected",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ErrorRed,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Alert triggered for admin intervention. The package scan does not match the expected route logic.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── Destination Target Card ──────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .drawLeftBorder(PrimaryContainer, 8.dp)
                        .padding(32.dp)
                ) {
                    Text(
                        "DESTINATION TARGET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        expectedCheckpoint,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = OnSurface
                    )
                    Text(
                        "Expected Checkpoint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Primary Logistics Hub",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CoralPrimary
                        )
                    }
                }

                // ── Actual Location Card (Error) ────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .drawLeftBorder(ErrorRed, 8.dp)
                        .padding(32.dp)
                ) {
                    Text(
                        "ACTUAL LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = ErrorRed
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        actualCheckpoint,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = ErrorRed
                    )
                    Text(
                        "Current Checkpoint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WrongLocation, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Route Discrepancy",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ErrorRed
                        )
                    }
                }

                // ── Package Info Glass Card ──────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Package image placeholder
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(ErrorContainer.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "PRIORITY EXPRESS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = OnErrorContainer
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(SurfaceContainer)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "ID: $orderId",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Premium Electronics Consignment",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Last valid scan: Munich Distribution Center (4h ago)",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Action Buttons ───────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SignatureGradient)
                            .clickable {
                                navController.navigate(Routes.STAFF_SCAN) {
                                    popUpTo(Routes.STAFF_HOME)
                                    launchSingleTop = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Acknowledge & Continue Scanning",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SurfaceContainerLow)
                            .clickable { /* TODO: Report to supervisor */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Report to Supervisor",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = OnSurface
                        )
                    }
                }

                Spacer(Modifier.height(80.dp))
            }

            // ── Floating Status Badge ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(InverseSurface)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "alertPulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "alertDot"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "TERMINAL STATUS: ALERT ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  STATE 3: DELIVERED
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun DeliveredState(
    navController: NavController,
    orderId: String,
    destinationHub: String
) {
    Scaffold(
        containerColor = Surface,
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.STAFF_HOME),
                    NavItem(
                        label = "Scan",
                        route = Routes.STAFF_SCAN,
                        iconContent = { isSelected -> StaffScanNavIcon(isSelected) }
                    ),
                    NavItem("History", Icons.Default.History, Routes.STAFF_HISTORY),
                ),
                currentRoute = "",
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.STAFF_HOME)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Glass Top Bar ────────────────────────────────────────────────
            StaffResultTopBar()

            // ── Hero Section ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SuccessTealContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "STATUS: FINAL DESTINATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = SuccessTeal
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Package\nDelivered!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 52.sp,
                        lineHeight = 56.sp,
                        brush = Brush.linearGradient(
                            listOf(SuccessTeal, Color(0xFF008B8B))
                        )
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Package successfully reached final destination and marked as Delivered. All logistics cycles for this item are completed.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                    color = OnSurfaceVariant,
                    lineHeight = 26.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Info Cards ───────────────────────────────────────────────────
            // Order Identifier
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .padding(24.dp)
            ) {
                Text(
                    "ORDER IDENTIFIER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Outline
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    orderId,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnSurface
                )
            }

            // Destination Hub
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .padding(24.dp)
            ) {
                Text(
                    "DESTINATION HUB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Outline
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = SuccessTeal, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        destinationHub,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Delivery Icon Visual ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle with teal tint
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(SuccessTealContainer.copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Inventory2,
                        null,
                        tint = SuccessTeal,
                        modifier = Modifier.size(96.dp)
                    )
                }
                // Check badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 60.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(SuccessTeal)
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Checkpoint Audit Trail ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(32.dp)
            ) {
                Text(
                    "Checkpoint Audit Trail",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnSurface
                )

                Spacer(Modifier.height(16.dp))

                // Trail items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(y = 6.dp)
                            .clip(CircleShape)
                            .background(SuccessTeal)
                    )
                    Column {
                        Text(
                            "Final Delivery Confirmation",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                        Text(
                            "North Hub Terminal • Just now",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(y = 6.dp)
                            .clip(CircleShape)
                            .background(Outline)
                    )
                    Column {
                        Text(
                            "In Transit to Destination",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                        Text(
                            "Central Distribution • 4h 12m ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Scan Next Parcel CTA ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryContainer)
                    .clickable {
                        navController.navigate(Routes.STAFF_SCAN) {
                            popUpTo(Routes.STAFF_HOME)
                            launchSingleTop = true
                        }
                    }
                    .padding(32.dp)
            ) {
                Column {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        null,
                        tint = OnPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Scan Next Parcel",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = OnPrimaryContainer
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ready for input...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnPrimaryContainer
                        )
                        Icon(Icons.Default.ArrowForward, null, tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Map background placeholder ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHighest.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    null,
                    tint = OnSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Shared Components
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StaffResultTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Menu, null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "The Kinetic Pulse",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = OnSurface
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
        }
    }
}

// ── Left Border Modifier ─────────────────────────────────────────────────────
private fun Modifier.drawLeftBorder(
    color: Color,
    width: androidx.compose.ui.unit.Dp
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(width.toPx(), size.height)
        )
    }
)
