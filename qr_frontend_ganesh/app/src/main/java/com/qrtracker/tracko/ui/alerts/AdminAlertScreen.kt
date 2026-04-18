package com.qrtracker.tracko.ui.alerts

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  Admin Alerts — Operational Alert Feed
//  Opened from bottom navigation "Alerts" tab
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── Alert Data Models ────────────────────────────────────────────────────────
enum class AlertSeverity { HIGH, MEDIUM, LOW }
enum class AlertStatus { OPEN, IN_PROGRESS, RESOLVED }
enum class AlertCategory { MISPLACED, ESCALATION, DUPLICATE, SYSTEM }

data class AdminAlert(
    val id: String,
    val title: String,
    val parcelId: String,
    val hubName: String,
    val timeAgo: String,
    val severity: AlertSeverity,
    val status: AlertStatus,
    val category: AlertCategory,
    val isUnread: Boolean = false
)

// ── Mock Data ────────────────────────────────────────────────────────────────
private val mockAdminAlerts = listOf(
    AdminAlert(
        "1", "Misplaced Parcel Detected", "QX-9902", "Berlin-BER",
        "2m ago", AlertSeverity.HIGH, AlertStatus.OPEN, AlertCategory.MISPLACED, true
    ),
    AdminAlert(
        "2", "Duplicate Scan Attempt", "QR-7729", "Berlin-BER",
        "15m ago", AlertSeverity.MEDIUM, AlertStatus.IN_PROGRESS, AlertCategory.DUPLICATE
    ),
    AdminAlert(
        "3", "Sensor Connection Dropped", "GW-04-BER", "Berlin-BER",
        "1h ago", AlertSeverity.LOW, AlertStatus.RESOLVED, AlertCategory.SYSTEM
    ),
)

@Composable
fun AdminAlertScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Misplaced", "Escalations", "Duplicate", "System")
    val alerts = remember { mockAdminAlerts }

    val filteredAlerts = remember(selectedFilter, alerts) {
        when (selectedFilter) {
            "All" -> alerts
            "Misplaced" -> alerts.filter { it.category == AlertCategory.MISPLACED }
            "Escalations" -> alerts.filter { it.category == AlertCategory.ESCALATION }
            "Duplicate" -> alerts.filter { it.category == AlertCategory.DUPLICATE }
            "System" -> alerts.filter { it.category == AlertCategory.SYSTEM }
            else -> alerts
        }
    }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.ADMIN_CHECKPOINT),
                    NavItem(
                        label = "Create",
                        route = Routes.ADMIN_CREATE_PACKAGE,
                        iconContent = { isSelected -> AdminCreateNavIcon(isSelected) }
                    ),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.ADMIN_PACKAGES),
                ),
                currentRoute = Routes.ADMIN_ALERTS,
                onItemClick = { route ->
                    when (route) {
                        Routes.ADMIN_ALERTS -> { /* already here */ }
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
            // ══════════════════════════════════════════════════════════════════
            //  Glass Header
            // ══════════════════════════════════════════════════════════════════
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
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Warehouse Control",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                brush = HorizontalBrandGradient
                            )
                        )
                    }

                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Editorial Header
            // ══════════════════════════════════════════════════════════════════
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        "SYSTEM PULSE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CoralPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Operational\nAlerts",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            lineHeight = 44.sp
                        ),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Real-time checkpoint monitoring and anomaly detection for Hub: Berlin-BER.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Filter Chips
            // ══════════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(
                                    if (isSelected) CoralPrimary else SurfaceContainerLowest
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) OnPrimary else OnSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ══════════════════════════════════════════════════════════════════
            //  Empty State
            // ══════════════════════════════════════════════════════════════════
            if (filteredAlerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldActiveBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Check, null, tint = EmeraldActive, modifier = Modifier.size(36.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "System Clear!",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "No alerts matching this filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Alert Cards
            // ══════════════════════════════════════════════════════════════════
            items(filteredAlerts) { alert ->
                OperationalAlertCard(
                    alert = alert,
                    onViewDetails = {
                        navController.navigate(
                            Routes.logisticsDashboard(alert.parcelId)
                        ) { launchSingleTop = true }
                    },
                    onResolve = { /* TODO */ },
                    onEscalate = { /* TODO */ },
                    onMarkResolved = { /* TODO */ }
                )
            }
        }
    }
}

// ── Operational Alert Card ───────────────────────────────────────────────────
@Composable
private fun OperationalAlertCard(
    alert: AdminAlert,
    onViewDetails: () -> Unit,
    onResolve: () -> Unit,
    onEscalate: () -> Unit,
    onMarkResolved: () -> Unit
) {
    val isResolved = alert.status == AlertStatus.RESOLVED

    val borderColor = when (alert.severity) {
        AlertSeverity.HIGH -> ErrorRed
        AlertSeverity.MEDIUM -> SecondaryContainer
        AlertSeverity.LOW -> SurfaceContainerHigh
    }

    val severityBg = when (alert.severity) {
        AlertSeverity.HIGH -> HighSeverityBg
        AlertSeverity.MEDIUM -> MediumSeverityBg
        AlertSeverity.LOW -> LowSeverityBg
    }

    val severityText = when (alert.severity) {
        AlertSeverity.HIGH -> HighSeverityText
        AlertSeverity.MEDIUM -> MediumSeverityText
        AlertSeverity.LOW -> LowSeverityText
    }

    val severityLabel = when (alert.severity) {
        AlertSeverity.HIGH -> "HIGH SEVERITY"
        AlertSeverity.MEDIUM -> "MEDIUM SEVERITY"
        AlertSeverity.LOW -> "LOW SEVERITY"
    }

    val statusColor = when (alert.status) {
        AlertStatus.OPEN -> ErrorRed
        AlertStatus.IN_PROGRESS -> CoralSecondary
        AlertStatus.RESOLVED -> ReceivedGreen
    }

    val statusLabel = when (alert.status) {
        AlertStatus.OPEN -> "Open"
        AlertStatus.IN_PROGRESS -> "In Progress"
        AlertStatus.RESOLVED -> "Resolved"
    }

    val statusIcon = when (alert.status) {
        AlertStatus.OPEN -> Icons.Default.Adjust
        AlertStatus.IN_PROGRESS -> Icons.Default.Sync
        AlertStatus.RESOLVED -> Icons.Default.CheckCircle
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .then(if (isResolved) Modifier.alpha(0.55f) else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isResolved) SurfaceContainerLow.copy(alpha = 0.5f) else SurfaceContainerLowest)
                .then(
                    if (!isResolved) Modifier.border(
                        width = 0.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ) else Modifier
                )
                .padding(start = 4.dp) // Space for left border
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left severity border
                if (!isResolved) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(borderColor)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp)
                ) {
                    // Severity + Time + Unread
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(severityBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    severityLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = severityText
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                alert.timeAgo,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = OnSurfaceVariant
                            )
                        }
                        // Unread ping
                        if (alert.isUnread) {
                            val infiniteTransition = rememberInfiniteTransition(label = "unreadPing")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.8f, targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                                label = "pingScale"
                            )
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size((12 * scale).dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed.copy(alpha = 0.3f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Title
                    Text(
                        alert.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = OnSurface
                    )

                    Spacer(Modifier.height(10.dp))

                    // Metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Inventory2, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                alert.parcelId,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                alert.hubName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    if (!isResolved) {
                        // Status
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                statusLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = statusColor
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Action Buttons
                        when (alert.severity) {
                            AlertSeverity.HIGH -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(SignatureGradient)
                                        .clickable { onViewDetails() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("View Details", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnPrimary)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(SurfaceContainerLow)
                                            .clickable { onResolve() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Resolve", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(SurfaceContainerLow)
                                            .clickable { onEscalate() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Escalate", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
                                    }
                                }
                            }
                            AlertSeverity.MEDIUM -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable { onViewDetails() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("View Details", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                                }
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(SurfaceContainerLow)
                                        .clickable { onMarkResolved() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Mark Resolved", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
                                }
                            }
                            AlertSeverity.LOW -> {
                                // No action buttons for resolved
                            }
                        }
                    } else {
                        // Resolved status
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = ReceivedGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Resolved",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ReceivedGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
