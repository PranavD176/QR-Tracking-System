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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.theme.Navy
import com.ganesh.qrtracker.ui.theme.ValidGreen
import com.ganesh.qrtracker.ui.theme.ValidGreenBg
import com.ganesh.qrtracker.ui.theme.MisplacedRed
import com.ganesh.qrtracker.ui.theme.MisplacedRedBg

// ── Mock scan history item — Member 2 replaces with real model ───────────────
data class ScanHistoryItem(
    val scanId       : String,
    val scannerName  : String,
    val result       : String,
    val location     : String,
    val scannedAt    : String
)

// ── Mock scan history — Member 2 replaces with ViewModel state ───────────────
private val mockScanHistory = listOf(
    ScanHistoryItem("s-001", "Rahul Sharma",  "valid",     "Library Room 3B",    "2026-04-01 10:30"),
    ScanHistoryItem("s-002", "Priya Patel",   "misplaced", "Cafeteria Block A",  "2026-04-02 14:15"),
    ScanHistoryItem("s-003", "Rahul Sharma",  "valid",     "Lab 204",            "2026-04-03 09:00"),
)

// ── State holder ─────────────────────────────────────────────────────────────
data class PackageDetailUiState(
    val description : String            = "Physics Textbook — Blue Cover",
    val status      : String            = "active",
    val createdAt   : String            = "2026-04-01",
    val qrPayload   : String            = "QR_TRACKING:uuid-001",
    val scanHistory : List<ScanHistoryItem> = mockScanHistory,
    val isLoading   : Boolean           = false,
    val error       : String?           = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    navController : NavController,
    packageId     : String
) {
    var uiState           by remember { mutableStateOf(PackageDetailUiState()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Load package detail on entry ─────────────────────────────────────────
    // TODO: replace with viewModel.loadPackage(packageId)
    LaunchedEffect(packageId) { /* mock — data already in uiState */ }

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Package Detail",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        when {
            // ── Loading ──────────────────────────────────────────────────────
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Navy) }
            }

            // ── Content ──────────────────────────────────────────────────────
            else -> {
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Package info card ────────────────────────────────────
                    item {
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(14.dp),
                            colors    = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint     = Navy,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text       = uiState.description,
                                        style      = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                DetailRow(label = "Package ID", value = packageId)
                                Spacer(Modifier.height(8.dp))
                                DetailRow(label = "Created",    value = uiState.createdAt)
                                Spacer(Modifier.height(8.dp))

                                // ── Status chip ──────────────────────────────
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text  = "Status",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val statusColor = when (uiState.status) {
                                        "active"    -> MaterialTheme.colorScheme.primary
                                        "misplaced" -> MaterialTheme.colorScheme.error
                                        else        -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = statusColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text       = uiState.status.replaceFirstChar { it.uppercase() },
                                            style      = MaterialTheme.typography.labelSmall,
                                            color      = statusColor,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── QR payload info ──────────────────────────────────────
                    item {
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(14.dp),
                            colors    = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text       = "QR Code Payload",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text  = uiState.qrPayload,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Navy,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                // TODO: replace Box below with real QR image
                                // generated via ZXing from uiState.qrPayload
                                // when Member 2 provides the payload from API
                                Box(
                                    modifier         = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(
                                            Color.LightGray.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text      = "QR Image will appear here\n(plugged in by Member 2)",
                                        style     = MaterialTheme.typography.bodySmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // ── Scan history header ──────────────────────────────────
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint     = Navy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text       = "Scan History",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // ── Empty scan history ───────────────────────────────────
                    if (uiState.scanHistory.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text      = "No scans yet",
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ── Scan history list ────────────────────────────────────
                    items(
                        items = uiState.scanHistory,
                        key   = { it.scanId }
                    ) { scan ->
                        ScanHistoryCard(scan = scan)
                    }
                }
            }
        }
    }
}

// ── Scan history card ─────────────────────────────────────────────────────────
@Composable
private fun ScanHistoryCard(scan: ScanHistoryItem) {
    val isValid     = scan.result == "valid"
    val accentColor = if (isValid) ValidGreen   else MisplacedRed
    val bgColor     = if (isValid) ValidGreenBg else MisplacedRedBg
    val icon        = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = scan.scannerName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = accentColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = scan.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = scan.scannedAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.6f)
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text       = if (isValid) "Valid" else "Misplaced",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Reusable detail row ───────────────────────────────────────────────────────
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface,
            modifier   = Modifier.weight(0.6f),
            textAlign  = TextAlign.End
        )
    }
}