package com.qrtracker.tracko.ui.alerts

import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
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
import com.qrtracker.tracko.ui.navigation.navigateWithState
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.viewmodel.AlertViewModel
import com.qrtracker.tracko.viewmodel.AcknowledgeState
import com.qrtracker.tracko.viewmodel.AlertListState
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

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

@Composable
fun AlertFeedScreen(
    navController: NavController,
    alertViewModel: AlertViewModel,
) {
    val context = LocalContext.current
    val alertListState by alertViewModel.alertListState.collectAsState()
    val acknowledgeState by alertViewModel.acknowledgeState.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Parcel", "Delivery", "System")
    var alerts by remember { mutableStateOf<List<UserAlert>>(emptyList()) }

    // Load alerts from API
    LaunchedEffect(Unit) {
        alertViewModel.fetchAlerts(null) // fetch all
    }

    // Map API response to UI model
    LaunchedEffect(alertListState) {
        if (alertListState is AlertListState.Success) {
            alerts = (alertListState as AlertListState.Success).alerts.map { a ->
                val (title, alertType) = when (a.alert_type) {
                    "acceptance_request" -> "New Package 📦" to UserAlertType.PARCEL_ARRIVED
                    "misplaced" -> "Misplaced Alert ⚠️" to UserAlertType.SECURITY
                    "rejected" -> "Parcel Rejected ❌" to UserAlertType.SECURITY
                    "out_of_sequence" -> "Sequence Breach ⚠️" to UserAlertType.SECURITY
                    "handoff" -> "Package Handoff 🚚" to UserAlertType.OUT_FOR_DELIVERY
                    else -> "Package Alert" to UserAlertType.PARCEL_ARRIVED
                }
                val description = when (a.alert_type) {
                    "rejected" -> a.location.ifBlank { "Package \"${a.package_description}\" was rejected" }
                    "out_of_sequence" -> a.location.ifBlank { "Package \"${a.package_description}\" scanned out of sequence" }
                    else -> "Package \"${a.package_description}\" scanned by ${a.scanned_by_name} at ${a.location}"
                }
                UserAlert(
                    id = a.alert_id,
                    title = title,
                    description = description,
                    type = alertType,
                    timeAgo = a.created_at.takeLast(8),
                    statusLabel = a.status.uppercase(),
                    isUnread = a.status == "sent"
                )
            }
        }
    }

    LaunchedEffect(acknowledgeState) {
        when (val state = acknowledgeState) {
            is AcknowledgeState.SuccessBulk -> {
                val message = if (state.updated > 0) {
                    "Marked ${state.updated} alerts as read"
                } else {
                    "No unread alerts"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                alertViewModel.resetAcknowledgeState()
            }
            is AcknowledgeState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                alertViewModel.fetchAlerts(null)
                alertViewModel.resetAcknowledgeState()
            }
            else -> Unit
        }
    }

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
                        navController.navigateWithState(route)
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
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GradientStart
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

