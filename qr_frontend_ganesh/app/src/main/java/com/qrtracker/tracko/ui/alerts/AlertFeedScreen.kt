package com.qrtracker.tracko.ui.alerts

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.ui.theme.GlassWhite
import com.qrtracker.tracko.ui.theme.HorizontalBrandGradient

// ── User Alert Data ──────────────────────────────────────────────────────────
enum class UserAlertType { PARCEL_ARRIVED, DELIVERY_DELAYED, OUT_FOR_DELIVERY, DELIVERED, SECURITY }

data class UserAlert(
    val id: String,
    val title: String,
    val description: String,
    val type: UserAlertType,
    val timeAgo: String,
    val statusLabel: String,
    val isUnread: Boolean = false
)

private val mockUserAlerts = listOf(
    UserAlert("1", "Package Arrived", "Your parcel QX-9902 is at the local hub.", UserAlertType.PARCEL_ARRIVED, "2m ago", "RECEIVED", true),
    UserAlert("2", "Delivery Delayed", "Weather conditions have impacted the delivery of PK-8829.", UserAlertType.DELIVERY_DELAYED, "1h ago", "ON HOLD"),
    UserAlert("3", "Out for Delivery", "Alex is on the way with your package QR-7712.", UserAlertType.OUT_FOR_DELIVERY, "3h ago", "ACTIVE"),
    UserAlert("4", "Delivered", "Package PK-4456 was left at your front door.", UserAlertType.DELIVERED, "Yesterday", "COMPLETED"),
    UserAlert("5", "Security Update", "Please update your password for better account protection.", UserAlertType.SECURITY, "2 days ago", "SECURITY"),
)

@Composable
fun AlertFeedScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Parcel", "Delivery", "System")
    var alerts by remember { mutableStateOf(mockUserAlerts) }

    val filteredAlerts = remember(selectedFilter, alerts) {
        when (selectedFilter) {
            "All" -> alerts
            "Parcel" -> alerts.filter { it.type == UserAlertType.PARCEL_ARRIVED || it.type == UserAlertType.DELIVERED }
            "Delivery" -> alerts.filter { it.type == UserAlertType.DELIVERY_DELAYED || it.type == UserAlertType.OUT_FOR_DELIVERY }
            "System" -> alerts.filter { it.type == UserAlertType.SECURITY }
            else -> alerts
        }
    }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.HOME),
                    NavItem("Scan", Icons.Default.QrCodeScanner, Routes.SCANNER),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.PACKAGE_LIST),
                    NavItem("Alerts", Icons.Default.Notifications, Routes.ALERTS),
                ),
                currentRoute = Routes.ALERTS,
                onItemClick = { route ->
                    if (route != Routes.ALERTS) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(GlassWhite).padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = OnSurface)
                        }
                        Text(
                            "Alerts",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            color = GradientStart
                        )
                    }
                    Text(
                        "Mark all as read",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GradientStart,
                        modifier = Modifier.clickable { alerts = alerts.map { it.copy(isUnread = false) } }
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSelected = filter == selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) }
                            )
                        }
                    }
                }
            }

            items(filteredAlerts) { alert ->
                UserAlertCard(alert = alert)
            }
        }
    }
}

@Composable
private fun UserAlertCard(alert: UserAlert) {
    val isCompleted = alert.type == UserAlertType.DELIVERED

    val icon: ImageVector = when (alert.type) {
        UserAlertType.PARCEL_ARRIVED -> Icons.Filled.LocalShipping
        UserAlertType.DELIVERY_DELAYED -> Icons.Filled.Warning
        UserAlertType.OUT_FOR_DELIVERY -> Icons.Filled.DirectionsRun
        UserAlertType.DELIVERED -> Icons.Filled.Inventory2
        UserAlertType.SECURITY -> Icons.Filled.Shield
    }

    val iconBg: Color = when (alert.type) {
        UserAlertType.PARCEL_ARRIVED -> ReceivedGreenBg
        UserAlertType.DELIVERY_DELAYED -> MisplacedOrangeBg
        UserAlertType.OUT_FOR_DELIVERY -> PaleBlue
        UserAlertType.DELIVERED -> SurfaceContainerHigh
        UserAlertType.SECURITY -> SurfaceContainer
    }

    val iconTint: Color = when (alert.type) {
        UserAlertType.PARCEL_ARRIVED -> ReceivedGreen
        UserAlertType.DELIVERY_DELAYED -> MisplacedOrange
        UserAlertType.OUT_FOR_DELIVERY -> Blue
        UserAlertType.DELIVERED -> OnSurfaceVariant
        UserAlertType.SECURITY -> DuplicateGray
    }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(if (isCompleted) SurfaceContainerLowest.copy(alpha = 0.6f) else SurfaceContainerLowest)
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(alert.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    Text(alert.timeAgo, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                Text(alert.description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        }
    }
}

