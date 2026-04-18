package com.qrtracker.tracko.ui.staff

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  Staff History — Scan History List for Checkpoint Staff
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

data class HistoryScanItem(
    val parcelId: String,
    val description: String,
    val timeAgo: String,
    val status: String // "success", "misplaced", "delivered"
)

private val mockHistory = listOf(
    HistoryScanItem("#KP-902-BX", "Successful Entry Scan", "2m ago", "success"),
    HistoryScanItem("#KP-114-AZ", "Outbound Verified", "12m ago", "success"),
    HistoryScanItem("#KP-445-TR", "Misplaced — Alert Triggered", "28m ago", "misplaced"),
    HistoryScanItem("#KP-770-MN", "Delivered to Final Hub", "1h ago", "delivered"),
    HistoryScanItem("#KP-331-QR", "Successful Entry Scan", "1h 22m ago", "success"),
    HistoryScanItem("#KP-220-LB", "Outbound Verified", "2h ago", "success"),
    HistoryScanItem("#KP-119-ZZ", "Misplaced — Rerouted", "2h 45m ago", "misplaced"),
    HistoryScanItem("#KP-008-YX", "Delivered to Final Hub", "3h ago", "delivered"),
)

@Composable
fun StaffHistoryScreen(navController: NavController) {
    val history = remember { mockHistory }

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
                currentRoute = Routes.STAFF_HISTORY,
                onItemClick = { route ->
                    when (route) {
                        Routes.STAFF_HISTORY -> { /* already here */ }
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Glass Top Bar ────────────────────────────────────────────────
            item {
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
                                .background(SurfaceContainerHigh)
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Scan History",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    brush = HorizontalBrandGradient
                                )
                            )
                            Text(
                                "All checkpoint scans",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // ── Stats summary ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HistoryStatChip(
                        count = history.count { it.status == "success" },
                        label = "Success",
                        color = ReceivedGreen,
                        bgColor = ReceivedGreenBg,
                        modifier = Modifier.weight(1f)
                    )
                    HistoryStatChip(
                        count = history.count { it.status == "misplaced" },
                        label = "Misplaced",
                        color = MisplacedOrange,
                        bgColor = MisplacedOrangeBg,
                        modifier = Modifier.weight(1f)
                    )
                    HistoryStatChip(
                        count = history.count { it.status == "delivered" },
                        label = "Delivered",
                        color = SuccessTeal,
                        bgColor = SuccessTealContainer.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Section Header ───────────────────────────────────────────────
            item {
                Text(
                    "TODAY",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = OnSurfaceVariant
                )
            }

            // ── Scan Items ───────────────────────────────────────────────────
            items(history) { item ->
                HistoryScanRow(item)
            }
        }
    }
}

@Composable
private fun HistoryStatChip(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

@Composable
private fun HistoryScanRow(item: HistoryScanItem) {
    val (iconBgColor, iconTint, statusIcon) = when (item.status) {
        "misplaced" -> Triple(MisplacedOrangeBg, MisplacedOrange, Icons.Outlined.WarningAmber)
        "delivered" -> Triple(SuccessTealContainer.copy(alpha = 0.3f), SuccessTeal, Icons.Filled.CheckCircle)
        else -> Triple(ReceivedGreenBg, ReceivedGreen, Icons.Outlined.Inventory2)
    }

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    item.parcelId,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Text(
            item.timeAgo,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurfaceVariant
        )
    }
}
