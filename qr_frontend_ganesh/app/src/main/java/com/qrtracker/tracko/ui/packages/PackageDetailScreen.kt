package com.qrtracker.tracko.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Search
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
import com.qrtracker.tracko.utils.QRCodeGenerator
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.PackageListState
import com.qrtracker.tracko.viewmodel.ScanHistoryState
import com.qrtracker.tracko.viewmodel.UpdateCheckpointsState
import com.qrtracker.tracko.viewmodel.UsersState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

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
    var pkgReceiverId by remember { mutableStateOf("") }
    var pkgReceiverName by remember { mutableStateOf("Unknown") }
    var pkgSenderId by remember { mutableStateOf("") }
    var pkgSenderName by remember { mutableStateOf("Unknown") }
    var routeCheckpoints by remember { mutableStateOf<List<String>>(emptyList()) }
    var pkgCreatedAt by remember { mutableStateOf("") }
    var showRouteEditor by remember { mutableStateOf(false) }
    // Checkpoint editor state — list of selected user IDs
    var selectedCheckpointIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val updateCheckpointsState by packageViewModel.updateCheckpointsState.collectAsState()
    val usersState by packageViewModel.usersState.collectAsState()
    var userSearchQuery by remember { mutableStateOf("") }

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
                pkgReceiverId = pkg.receiver_id ?: ""
                pkgReceiverName = pkg.receiver_name ?: "Unknown"
                pkgSenderId = pkg.sender_id ?: ""
                pkgSenderName = pkg.sender_name ?: "Unknown"
                routeCheckpoints = pkg.route_checkpoints ?: emptyList()
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

    LaunchedEffect(updateCheckpointsState) {
        when (val state = updateCheckpointsState) {
            is UpdateCheckpointsState.Success -> {
                showRouteEditor = false
                packageViewModel.fetchPackages()
                packageViewModel.resetUpdateCheckpointsState()
                Toast.makeText(context, "Route checkpoints updated", Toast.LENGTH_SHORT).show()
            }
            is UpdateCheckpointsState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                packageViewModel.resetUpdateCheckpointsState()
            }
            else -> Unit
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
            //  Pending Acceptance Actions
            // ══════════════════════════════════════════════════════════════════
            if (pkgStatus == "pending_acceptance" && pkgReceiverId == tokenManager.getUserId()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CoralPrimary.copy(alpha = 0.1f))
                        .border(1.dp, CoralPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Accept this parcel?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You need to accept this parcel before the sender can start tracking it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { packageViewModel.rejectPackage(packageId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerHigh,
                                contentColor = StatusRedText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = { packageViewModel.acceptPackage(packageId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .padding(20.dp)
                ) {
                    Column {
                        EditorialLabel(text = "Route")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Receiver: $pkgReceiverName",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                        if (routeCheckpoints.isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "No intermediate checkpoints configured.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        } else {
                            Spacer(Modifier.height(8.dp))
                            routeCheckpoints.forEachIndexed { index, checkpoint ->
                                Text(
                                    text = "${index + 1}. $checkpoint",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
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
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val content = pkgTrackingId.ifBlank { packageId }
                                val bitmap = QRCodeGenerator.generate(content)
                                if (bitmap != null) {
                                    val saved = QRCodeGenerator.saveToGallery(context, bitmap, "qr_$packageId")
                                    Toast.makeText(
                                        context,
                                        if (saved) "QR saved to gallery" else "Failed to save QR",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "Unable to generate QR", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download QR")
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            if (routeCheckpoints.isNotEmpty()) {
                val validScanCount = scanEntries.count { it.result == "valid" }
                val covered = validScanCount.coerceAtMost(routeCheckpoints.size)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLowest)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Route Covered: $covered/${routeCheckpoints.size}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (pkgSenderId == tokenManager.getUserId()) {
                Button(
                    onClick = {
                        selectedCheckpointIds = routeCheckpoints
                        userSearchQuery = ""
                        packageViewModel.fetchUsers()
                        showRouteEditor = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Edit Remaining Route")
                }
                Spacer(Modifier.height(16.dp))
            }

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

    // ── Route Editor Dialog with User Picker ─────────────────────────────────
    if (showRouteEditor) {
        val allUsers = (usersState as? UsersState.Success)?.users ?: emptyList()
        val filteredUsers = remember(userSearchQuery, allUsers) {
            if (userSearchQuery.isBlank()) allUsers
            else allUsers.filter {
                it.full_name.contains(userSearchQuery, ignoreCase = true) ||
                it.email.contains(userSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showRouteEditor = false },
            title = { Text("Edit Route Checkpoints") },
            text = {
                Column {
                    Text(
                        text = "Select users who will handle this parcel en route to $pkgReceiverName.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    // ── Selected chips ────────────────────────────────────────
                    if (selectedCheckpointIds.isNotEmpty()) {
                        Text(
                            "Selected (${selectedCheckpointIds.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedCheckpointIds.forEach { uid ->
                                val user = allUsers.firstOrNull { it.user_id == uid }
                                val label = user?.full_name ?: uid
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CoralPrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = CoralPrimary
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = CoralPrimary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                selectedCheckpointIds = selectedCheckpointIds.filter { it != uid }
                                            }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = SurfaceContainerHighest)
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── User search field ─────────────────────────────────────
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = {
                            userSearchQuery = it
                            packageViewModel.fetchUsers(it.ifBlank { null })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search users…") },
                        leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    // ── User list ─────────────────────────────────────────────
                    when {
                        usersState is UsersState.Loading -> {
                            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CoralPrimary)
                            }
                        }
                        filteredUsers.isEmpty() -> {
                            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text("No users found", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredUsers) { user ->
                                    val isSelected = user.user_id in selectedCheckpointIds
                                    val isReceiver = user.user_id == pkgReceiverId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) CoralPrimary.copy(alpha = 0.1f)
                                                else SurfaceContainerLow
                                            )
                                            .clickable(enabled = !isReceiver) {
                                                selectedCheckpointIds = if (isSelected) {
                                                    selectedCheckpointIds.filter { it != user.user_id }
                                                } else {
                                                    selectedCheckpointIds + user.user_id
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                user.full_name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isReceiver) OnSurfaceVariant else OnSurface
                                            )
                                            Text(
                                                user.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                        if (isReceiver) {
                                            Text(
                                                "Receiver",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = OnSurfaceVariant
                                            )
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Check, null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedCheckpointIds.isEmpty()) {
                            Toast.makeText(context, "Select at least one checkpoint user", Toast.LENGTH_SHORT).show()
                        } else {
                            packageViewModel.updateCheckpoints(packageId, selectedCheckpointIds)
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRouteEditor = false }) { Text("Cancel") }
            }
        )
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
