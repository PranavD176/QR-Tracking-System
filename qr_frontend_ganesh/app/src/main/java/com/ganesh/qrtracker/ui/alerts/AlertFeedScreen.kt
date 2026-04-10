package com.ganesh.qrtracker.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Warning
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
import com.ganesh.qrtracker.network.models.AlertResponse
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.*
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.AlertListState
import com.ganesh.qrtracker.viewmodel.AlertViewModel

@Composable
fun AlertFeedScreen(
    navController: NavController,
    tokenManager: TokenManager,
    onSessionExpired: () -> Unit   // Called when 401 received — navigate to login
) {
    val viewModel = remember { AlertViewModel(tokenManager) }
    val alertListState by viewModel.alertListState.collectAsState()
    val acknowledgeState by viewModel.acknowledgeState.collectAsState()

    // Fetch alerts when screen first loads
    LaunchedEffect(Unit) {
        viewModel.fetchAlerts()
    }

    // If acknowledge succeeds, reset that state
    LaunchedEffect(acknowledgeState) {
        if (acknowledgeState is com.ganesh.qrtracker.viewmodel.AcknowledgeState.Success) {
            viewModel.resetAcknowledgeState()
        }
    }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.PACKAGE_LIST),
                    NavItem("Scan", Icons.Default.QrCodeScanner, Routes.SCANNER),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.PACKAGE_LIST),
                    NavItem("Alerts", Icons.Default.Notifications, Routes.ALERTS),
                ),
                currentRoute = Routes.ALERTS,
                onItemClick = { route ->
                    if (route != Routes.ALERTS) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Glass Top Bar ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Alerts",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        brush = HorizontalBrandGradient
                    )
                )
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

            when (val state = alertListState) {

                is AlertListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CoralPrimary)
                    }
                }

                is AlertListState.Success -> {
                    if (state.alerts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = EmeraldActive,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "All Clear!",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = OnSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "No active alerts. All packages are safe.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.alerts) { alert ->
                                AlertCard(
                                    alert = alert,
                                    onAcknowledge = { viewModel.acknowledgeAlert(alert.alert_id) }
                                )
                            }
                        }
                    }
                }

                is AlertListState.Error -> {
                    if (state.message.contains("Session expired")) {
                        onSessionExpired()
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.message,
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                GradientButton(
                                    text = "Retry",
                                    onClick = { viewModel.fetchAlerts() },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertResponse,
    onAcknowledge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceContainerLowest)
            .padding(20.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusRedChip),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = StatusRedText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = alert.package_description,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface
                    )
                    Text(
                        text = "Misplaced Alert",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusRedText
                    )
                }
            }
            StatusChip(status = "misplaced")
        }

        Spacer(Modifier.height(14.dp))

        // Details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceContainerHigh)
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                EditorialLabel(text = "Scanned By")
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.scanned_by_name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                EditorialLabel(text = "Location")
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.location,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = alert.created_at,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = OnSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(16.dp))

        // Acknowledge button
        GradientButton(
            text = "Acknowledge",
            onClick = onAcknowledge,
            modifier = Modifier.height(44.dp)
        )
    }
}