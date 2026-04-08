package com.ganesh.qrtracker.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.qrtracker.network.models.ScanHistoryResponse
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.PackageViewModel
import com.ganesh.qrtracker.viewmodel.ScanHistoryState

@Composable
fun PackageDetailScreen(
    packageId: String,
    packageDescription: String,
    tokenManager: TokenManager,
    onSessionExpired: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel = remember { PackageViewModel(tokenManager) }
    val scanHistoryState by viewModel.scanHistoryState.collectAsState()

    // Fetch scan history when screen loads
    LaunchedEffect(packageId) {
        viewModel.fetchScanHistory(packageId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        TextButton(onClick = onBack) {
            Text("← Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Package name header
        Text(
            text = packageDescription,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Scan History",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = scanHistoryState) {

            is ScanHistoryState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ScanHistoryState.Success -> {
                if (state.scans.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scans recorded for this package yet.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.scans) { scan ->
                            ScanHistoryCard(scan = scan)
                        }
                    }
                }
            }

            is ScanHistoryState.Error -> {
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
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(onClick = {
                                viewModel.fetchScanHistory(packageId)
                            }) {
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
fun ScanHistoryCard(scan: ScanHistoryResponse) {
    // Color code by result — green for valid, red for misplaced
    val cardColor = if (scan.result == "valid") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val resultColor = if (scan.result == "valid") Color(0xFF388E3C) else Color(0xFFC62828)
    val resultLabel = if (scan.result == "valid") "✅ Valid" else "🚨 Misplaced"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = scan.scanner_name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = resultLabel,
                    color = resultColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📍 ${scan.location_description}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Text(
                text = "🕒 ${scan.scanned_at}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}