package com.qrtracker.tracko.ui.staff

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
//  Staff Home — Checkpoint Staff Landing Screen
//  Pixel-accurate to: stitch_qr_tracker_app_ui/staff_home
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── Data Models (ViewModel-ready) ────────────────────────────────────────────
data class StaffHomeUiState(
    val staffName: String = "Terminal Manager",
    val staffRole: String = "Active Duty",
    val checkpointName: String = "Berlin Hub (BER)",
    val checkpointDescription: String = "Strategic North Hub Terminal • Gate A-14 Logistics Center",
    val packagesScanned: Int = 1248,
    val shiftTimeElapsed: String = "06:42",
    val isSystemReady: Boolean = true,
    val recentActivity: List<StaffRecentScan> = emptyList()
)

data class StaffRecentScan(
    val parcelId: String,
    val description: String,
    val timeAgo: String
)

// ── Mock Data ────────────────────────────────────────────────────────────────
private val mockRecentScans = listOf(
    StaffRecentScan("#KP-902-BX", "Successful Entry Scan", "2m ago"),
    StaffRecentScan("#KP-114-AZ", "Outbound Verified", "12m ago"),
)

@Composable
fun StaffHomeScreen(navController: NavController) {
    // TODO: Replace with ViewModel state
    val uiState = remember {
        StaffHomeUiState(recentActivity = mockRecentScans)
    }

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
                currentRoute = Routes.STAFF_HOME,
                onItemClick = { route ->
                    when (route) {
                        Routes.STAFF_HOME -> { /* already here */ }
                        else -> navController.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ══════════════════════════════════════════════════════════════════
            //  Glass Top Bar
            // ══════════════════════════════════════════════════════════════════
            item {
                StaffTopBar(
                    uiState = uiState,
                    onProfileClick = {
                        navController.navigate(Routes.CHECKPOINT_PROFILE) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            //  Hero Checkpoint Visual
            // ══════════════════════════════════════════════════════════════════
            item {
                StaffCheckpointHero(uiState)
            }

            // ══════════════════════════════════════════════════════════════════
            //  Large Scan Button
            // ══════════════════════════════════════════════════════════════════
            item {
                StaffScanButton(
                    isReady = uiState.isSystemReady,
                    onClick = {
                        navController.navigate(Routes.STAFF_SCAN) { launchSingleTop = true }
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            //  Stats Bento Grid
            // ══════════════════════════════════════════════════════════════════
            item {
                StaffStatsGrid(uiState)
            }

            // ══════════════════════════════════════════════════════════════════
            //  Recent Activity Section
            // ══════════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        "View History",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = CoralPrimary,
                        modifier = Modifier.clickable {
                            navController.navigate(Routes.STAFF_HISTORY) { launchSingleTop = true }
                        }
                    )
                }
            }

            items(uiState.recentActivity) { scan ->
                RecentScanItem(scan)
            }
        }
    }
}

// ── Glass Top Bar ────────────────────────────────────────────────────────────
@Composable
private fun StaffTopBar(
    uiState: StaffHomeUiState,
    onProfileClick: () -> Unit
) {
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
                Icon(Icons.Default.Menu, null, tint = CoralPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "The Kinetic Pulse",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = OnSurface
                )
                Text(
                    uiState.checkpointName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHighest)
                .border(2.dp, PrimaryContainer, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = CoralPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

// ── Hero Checkpoint Card ─────────────────────────────────────────────────────
@Composable
private fun StaffCheckpointHero(uiState: StaffHomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative blur circles
        Box(
            modifier = Modifier
                .size(128.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(PrimaryContainer.copy(alpha = 0.1f), CircleShape)
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 60.dp, y = 40.dp)
                .background(SecondaryContainer.copy(alpha = 0.1f), CircleShape)
                .blur(48.dp)
        )

        // Main card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location icon circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "ASSIGNED CHECKPOINT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = CoralPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                uiState.checkpointName,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    lineHeight = 38.sp
                ),
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                uiState.checkpointDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Large Scan Button ────────────────────────────────────────────────────────
@Composable
private fun StaffScanButton(
    isReady: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scanScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large circular scan button
        Box(
            modifier = Modifier
                .size(256.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(SignatureGradient)
                .clickable {
                    isPressed = true
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Inner glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = "Start Scanning",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "START SCANNING",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // System ready indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isReady) {
                val infiniteTransition = rememberInfiniteTransition(label = "readyPulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(CoralPrimary.copy(alpha = alpha))
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "System Ready for Input",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurfaceVariant
            )
        }
    }
}

// ── Stats Bento Grid ─────────────────────────────────────────────────────────
@Composable
private fun StaffStatsGrid(uiState: StaffHomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Packages Scanned
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest)
                .padding(20.dp)
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                uiState.packagesScanned.toString().let {
                    if (it.length > 3) "${it.substring(0, it.length - 3)},${it.substring(it.length - 3)}"
                    else it
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                ),
                color = OnSurface
            )
            Text(
                "Packages Scanned",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = OnSurfaceVariant
            )
        }

        // Shift Time
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest)
                .padding(20.dp)
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = CoralSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                uiState.shiftTimeElapsed,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                ),
                color = OnSurface
            )
            Text(
                "Shift Time Elapsed",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = OnSurfaceVariant
            )
        }
    }
}

// ── Recent Scan Item ─────────────────────────────────────────────────────────
@Composable
private fun RecentScanItem(scan: StaffRecentScan) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Inventory2, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    scan.parcelId,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    scan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Text(
            scan.timeAgo,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurfaceVariant
        )
    }
}
