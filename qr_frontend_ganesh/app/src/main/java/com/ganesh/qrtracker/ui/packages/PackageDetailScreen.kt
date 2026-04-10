package com.ganesh.qrtracker.ui.packages

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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ganesh.qrtracker.network.models.ScanHistoryResponse
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.*
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.PackageViewModel
import com.ganesh.qrtracker.viewmodel.ScanHistoryState

@Composable
fun PackageDetailScreen(
    navController: NavController,
    packageId: String
) {
    val scrollState = rememberScrollState()
    // Mock data — Member 2 will replace with ViewModel state
    val mockDescription = "MacBook Pro 16\""
    val mockTrackingId = "QRT-8829-XL"
    val mockStatus = "active"

    // Mock scan history
    val mockScans = listOf(
        MockScanEntry("Distribution Center, San Francisco", "Alex Johnson", "valid", "Oct 23, 2023 11:42 AM"),
        MockScanEntry("Warehouse B, Oakland", "Maria Garcia", "valid", "Oct 22, 2023 09:15 AM"),
        MockScanEntry("Airport Terminal, SFO", "System Scanner", "misplaced", "Oct 21, 2023 05:30 PM"),
    )

    Scaffold(
        containerColor = Surface,
        topBar = {
            GlassTopBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                },
                actions = {
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
            )
        },
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.PACKAGE_LIST),
                    NavItem("Scan", Icons.Default.QrCodeScanner, Routes.SCANNER),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.PACKAGE_LIST),
                    NavItem("Alerts", Icons.Default.Notifications, Routes.ALERTS),
                ),
                currentRoute = Routes.PACKAGE_LIST,
                onItemClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // ══════════════════════════════════════════════════════════════════
            //  Gradient Hero Section
            // ══════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(SignatureGradient)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column {
                    // Status pill
                    StatusChip(status = mockStatus)
                    Spacer(Modifier.height(12.dp))

                    // Package name
                    Text(
                        text = mockDescription,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Express Shipping",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ══════════════════════════════════════════════════════════════════
            //  Bento Grid — Info Cards
            // ══════════════════════════════════════════════════════════════════
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // ── Tracking ID Card ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .padding(20.dp)
                ) {
                    Column {
                        EditorialLabel(text = "Tracking Number")
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mockTrackingId,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = OnSurface
                            )
                            IconButton(
                                onClick = { /* TODO: Copy to clipboard */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // ── Metadata Row ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetadataCard(
                        label = "Created",
                        value = "Oct 20, '23",
                        modifier = Modifier.weight(1f)
                    )
                    MetadataCard(
                        label = "Weight",
                        value = "2.4 kg",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ── QR Code Card ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EditorialLabel(text = "QR Identifier")
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "QR_TRACKING:$packageId",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ══════════════════════════════════════════════════════════════════
            //  Movement Pulse Timeline
            // ══════════════════════════════════════════════════════════════════
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Movement Pulse",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                mockScans.forEachIndexed { index, scan ->
                    Row(Modifier.fillMaxWidth()) {
                        // Timeline indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(40.dp)
                        ) {
                            val dotColor = if (scan.result == "valid") EmeraldActive else StatusRedText
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            if (index < mockScans.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(80.dp)
                                        .background(SurfaceContainerHigh)
                                )
                            }
                        }

                        // Scan card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLowest)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = scan.location,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = if (scan.result == "valid") "✓" else "⚠",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (scan.result == "valid") EmeraldActive else StatusRedText
                                        )
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "by ${scan.scannerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = scan.timestamp,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    if (index < mockScans.size - 1) Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(100.dp)) // Bottom nav clearance
        }
    }
}

// ── Supporting Data & Composables ────────────────────────────────────────────

private data class MockScanEntry(
    val location: String,
    val scannerName: String,
    val result: String,
    val timestamp: String
)

@Composable
private fun MetadataCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp)
    ) {
        EditorialLabel(text = label)
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = OnSurface
        )
    }
}