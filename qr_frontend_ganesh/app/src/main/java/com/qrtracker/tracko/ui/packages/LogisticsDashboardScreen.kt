package com.qrtracker.tracko.ui.packages

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  Logistics Dashboard — Admin Package Detail with Dynamic Status
//  Pixel-accurate to: stitch_qr_tracker_app_ui/package_details_logistics_Admin
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── Data Models (ViewModel-ready) ────────────────────────────────────────────
data class LogisticsUiState(
    val orderId: String = "QR-7729-LX-04",
    val isMisplaced: Boolean = true,
    val source: String = "Milan Hub (MXP)",
    val destination: String = "Berlin Hub (BER)",
    val currentLocation: String = "Leipzig Distribution Center",
    val expectedCheckpoint: String = "BER-Terminal 2",
    val scannedCheckpoint: String = "LEJ-Sorting A",
    val incidentTimestamp: String = "Oct 25, 23:15:04",
    val routeStops: List<RouteStop> = emptyList()
)

data class RouteStop(
    val code: String,
    val isPassed: Boolean,
    val isError: Boolean = false,
    val isCurrent: Boolean = false
)

// ── Mock Data ────────────────────────────────────────────────────────────────
private val mockRouteStops = listOf(
    RouteStop("MXP", isPassed = true),
    RouteStop("MUC", isPassed = true),
    RouteStop("LEJ", isPassed = true, isError = true, isCurrent = true),
    RouteStop("BER", isPassed = false),
)

@Composable
fun LogisticsDashboardScreen(
    navController: NavController,
    packageId: String
) {
    // TODO: Replace with ViewModel fetch using packageId
    val uiState = remember {
        LogisticsUiState(
            orderId = packageId.ifBlank { "QR-7729-LX-04" },
            routeStops = mockRouteStops
        )
    }

    Scaffold(containerColor = Surface) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                }
                Text(
                    "Logistics Dashboard",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = OnSurface
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable { /* TODO: More options */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Order Info Card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "ORDER ID",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Outline
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            uiState.orderId,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                lineHeight = 34.sp
                            ),
                            color = OnSurface
                        )
                    }
                    // Dynamic Status Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(
                                if (uiState.isMisplaced) MisplacedRedStatusBg
                                else OnTrackGreenBg
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (uiState.isMisplaced) "MISPLACED" else "ON TRACK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (uiState.isMisplaced) MisplacedRedStatus else OnTrackGreen
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "SOURCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Outline
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            uiState.source,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "DESTINATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Outline
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            uiState.destination,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "CURRENT LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Outline
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = if (uiState.isMisplaced) MisplacedRedStatus else OnTrackGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            uiState.currentLocation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Exception Detected Section (only for misplaced) ───────────────
            if (uiState.isMisplaced) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF991B1B))
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "EXCEPTION DETECTED",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Expected Checkpoint row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Expected Checkpoint",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            uiState.expectedCheckpoint,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Spacer(Modifier.height(8.dp))

                    // Scanned Checkpoint row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Scanned Checkpoint",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            uiState.scannedCheckpoint,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Spacer(Modifier.height(8.dp))

                    // Timestamp row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Incident Timestamp",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                        Text(
                            uiState.incidentTimestamp,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Visual Route Progress ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "VISUAL ROUTE PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = OnSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    // Route dots and connections
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.routeStops.forEachIndexed { index, stop ->
                            // Dot
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val dotColor = when {
                                    stop.isError -> MisplacedRedStatus
                                    stop.isPassed -> CoralPrimary
                                    else -> SurfaceContainerHighest
                                }
                                val dotSize = if (stop.isError) 20.dp else 12.dp

                                Box(contentAlignment = Alignment.Center) {
                                    if (stop.isError) {
                                        Box(
                                            modifier = Modifier
                                                .size(dotSize)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                                .border(3.dp, MisplacedRedStatusBg, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(dotSize)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stop.code,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (stop.isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    color = if (stop.isError) MisplacedRedStatus
                                    else if (stop.isPassed) OnSurface
                                    else OnSurfaceVariant
                                )
                            }

                            // Connection line
                            if (index < uiState.routeStops.size - 1) {
                                val nextStop = uiState.routeStops[index + 1]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .offset(y = (-12).dp)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(
                                            if (stop.isPassed && nextStop.isPassed)
                                                if (stop.isError || nextStop.isError) MisplacedRedStatus
                                                else CoralPrimary
                                            else if (stop.isPassed)
                                                if (nextStop.isError) MisplacedRedStatus
                                                else CoralPrimary.copy(alpha = 0.5f)
                                            else SurfaceContainerHighest
                                        )
                                )
                            }
                        }
                    }

                    if (uiState.isMisplaced) {
                        Spacer(Modifier.height(24.dp))

                        // Misrouted message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLow)
                                .padding(16.dp)
                        ) {
                            val errorStop = uiState.routeStops.find { it.isError }
                            Text(
                                "Package misrouted at ${errorStop?.code ?: "unknown"}. Corrective action required.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Edit Remaining Route Button (only for misplaced) ─────────────
            if (uiState.isMisplaced) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF1C1917))
                            .clickable { /* TODO: Navigate to route editor */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Edit Remaining Route",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "* Only future checkpoints can be modified.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
