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
import com.ganesh.qrtracker.network.models.AdminAlertResponse
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.AdminAlertState
import com.ganesh.qrtracker.viewmodel.AlertViewModel

@Composable
fun AdminAlertScreen(
    tokenManager: TokenManager,
    onSessionExpired: () -> Unit,
    onAccessDenied: () -> Unit    // Called if non-admin somehow reaches this screen
) {
    val viewModel = remember { AlertViewModel(tokenManager) }
    val adminAlertState by viewModel.adminAlertState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdminAlerts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin — All Alerts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = adminAlertState) {

            is AdminAlertState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AdminAlertState.Success -> {
                if (state.alerts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No unacknowledged alerts system-wide. ✅",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.alerts) { alert ->
                            AdminAlertCard(alert = alert)
                        }
                    }
                }
            }

            is AdminAlertState.Error -> {
                when {
                    state.message.contains("Session expired") -> onSessionExpired()
                    state.message.contains("Access denied") -> onAccessDenied()
                    else -> {
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
                                Button(onClick = { viewModel.fetchAdminAlerts() }) {
                                    Text("Retry")
                                }
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
fun AdminAlertCard(alert: AdminAlertResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE) // Light red — admin urgency
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "📦 ${alert.package_description}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Owner: ${alert.owner_name}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Scanned by: ${alert.scanned_by_name}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Location: ${alert.location}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Time: ${alert.created_at}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}