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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.PackageListState
import com.qrtracker.tracko.viewmodel.ScanHistoryState
import androidx.compose.ui.platform.LocalContext

@Composable
fun PackageDetailScreen(
    navController: NavController,
    packageId: String
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val packageViewModel = remember { PackageViewModel(tokenManager) }
    val pkgListState by packageViewModel.packageListState.collectAsState()
    val scanHistoryState by packageViewModel.scanHistoryState.collectAsState()

    // Package detail state from API
    var pkgDescription by remember { mutableStateOf("Loading...") }
    var pkgTrackingId by remember { mutableStateOf(if (packageId.length > 16) packageId.take(8) + "..." + packageId.takeLast(4) else packageId) }
    var pkgStatus by remember { mutableStateOf("in_transit") }
    var pkgCreatedAt by remember { mutableStateOf("") }

    // Scan history from API
    var scanEntries by remember { mutableStateOf<List<MockScanEntry>>(emptyList()) }

    // Fetch data on entry
    LaunchedEffect(packageId) {
        packageViewModel.fetchPackages()
        packageViewModel.fetchScanHistory(packageId)
    }

    // Observe package list to extract this package's details
    LaunchedEffect(pkgListState) {
        if (pkgListState is PackageListState.Success) {
            val pkg = (pkgListState as PackageListState.Success).packages
                .firstOrNull { it.package_id == packageId }
            if (pkg != null) {
                pkgDescription = pkg.description
                pkgTrackingId = run {
                    val fullId = pkg.qr_payload ?: pkg.package_id
                    if (fullId.length > 16) fullId.take(8) + "..." + fullId.takeLast(4) else fullId
                }
                pkgStatus = pkg.status
                pkgCreatedAt = pkg.created_at ?: ""
            }
        }
    }

    // Observe scan history
    LaunchedEffect(scanHistoryState) {
        if (scanHistoryState is ScanHistoryState.Success) {
            scanEntries = (scanHistoryState as ScanHistoryState.Success).scans.map { s ->
                MockScanEntry(
                    location = s.location_description,
                    scannerName = s.scanner_name,
                    result = s.result.lowercase(),
                    timestamp = s.scanned_at
                )
            }
        }
    }

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
                    StatusChip(status = pkgStatus)
                    Spacer(Modifier.height(12.dp))

                    // Package name
                    Text(
                        text = pkgDescription,
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
                                text = pkgTrackingId,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = OnSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                        value = if (pkgCreatedAt.length >= 10) pkgCreatedAt.take(10) else pkgCreatedAt,
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
                            text = "QR_TRACKING:${if (packageId.length > 16) packageId.take(8) + "..." else packageId}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

                scanEntries.forEachIndexed { index, scan ->
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
                            if (index < scanEntries.size - 1) {
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
                    if (index < scanEntries.size - 1) Spacer(Modifier.height(12.dp))
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
