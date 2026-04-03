package com.ganesh.qrtracker.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.qrtracker.network.models.AlertResponse
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.AlertListState
import com.ganesh.qrtracker.viewmodel.AlertViewModel

@Composable
fun AlertFeedScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Alerts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = alertListState) {

            is AlertListState.Loading -> {
                // Show spinner in center while fetching
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AlertListState.Success -> {
                if (state.alerts.isEmpty()) {
                    // No alerts — show friendly empty message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active alerts. All packages are safe! ✅",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // Show list of alert cards
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                // Check if session expired — redirect to login
                if (state.message.contains("Session expired")) {
                    onSessionExpired()
                } else {
                    // Show error with retry button
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(onClick = { viewModel.fetchAlerts() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertResponse,
    onAcknowledge: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0) // Light orange — signals attention
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Package name
            Text(
                text = "📦 ${alert.package_description}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Who scanned it
            Text(
                text = "Scanned by: ${alert.scanned_by_name}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            // Where it was scanned
            Text(
                text = "Location: ${alert.location}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            // When it happened
            Text(
                text = "Time: ${alert.created_at}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Acknowledge button
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C) // Green
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Acknowledge", color = Color.White)
            }
        }
    }
}