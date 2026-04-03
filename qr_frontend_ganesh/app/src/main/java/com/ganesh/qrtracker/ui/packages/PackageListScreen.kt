package com.ganesh.qrtracker.ui.packages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.Navy

// ── Mock data class — Member 2 replaces with real model ──────────────────────
data class PackageItem(
    val packageId   : String,
    val description : String,
    val status      : String,
    val createdAt   : String
)

// ── Mock list — Member 2 replaces with ViewModel state ───────────────────────
private val mockPackages = listOf(
    PackageItem("uuid-001", "Physics Textbook — Blue Cover",  "active",    "2026-04-01"),
    PackageItem("uuid-002", "Lab Equipment Box",              "active",    "2026-04-02"),
    PackageItem("uuid-003", "Project Files — Semester 4",     "misplaced", "2026-04-03"),
    PackageItem("uuid-004", "Personal Bag — Black",           "completed", "2026-03-28"),
)

// ── State holder ─────────────────────────────────────────────────────────────
data class PackageListUiState(
    val packages    : List<PackageItem> = mockPackages,
    val isLoading   : Boolean           = false,
    val isRefreshing: Boolean           = false,
    val error       : String?           = null,
    val showLogout  : Boolean           = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageListScreen(navController: NavController) {

    var uiState           by remember { mutableStateOf(PackageListUiState()) }
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    // ── Logout confirmation dialog ────────────────────────────────────────────
    if (uiState.showLogout) {
        AlertDialog(
            onDismissRequest = { uiState = uiState.copy(showLogout = false) },
            title   = { Text("Logout") },
            text    = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    uiState = uiState.copy(showLogout = false)
                    // TODO: replace with viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    uiState = uiState.copy(showLogout = false)
                }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "My Packages",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                ),
                actions = {
                    // ── Scan button in top bar ───────────────────────────────
                    IconButton(onClick = {
                        navController.navigate(Routes.SCANNER)
                    }) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // ── Logout button ────────────────────────────────────────
                    IconButton(onClick = {
                        uiState = uiState.copy(showLogout = true)
                    }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { navController.navigate(Routes.CREATE_PACKAGE) },
                containerColor     = Navy,
                contentColor       = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Package")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        when {
            // ── Loading state ────────────────────────────────────────────────
            uiState.isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Navy)
                }
            }

            // ── Empty state ──────────────────────────────────────────────────
            uiState.packages.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier           = Modifier.size(72.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text       = "No packages yet",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "Tap + to create your first package",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant
                                .copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Package list ─────────────────────────────────────────────────
            else -> {
                LazyColumn(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.packages,
                        key   = { it.packageId }
                    ) { pkg ->
                        PackageCard(
                            pkg     = pkg,
                            onClick = {
                                navController.navigate(
                                    Routes.packageDetail(pkg.packageId)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Package card ──────────────────────────────────────────────────────────────
@Composable
private fun PackageCard(
    pkg     : PackageItem,
    onClick : () -> Unit
) {
    val statusColor = when (pkg.status) {
        "active"    -> MaterialTheme.colorScheme.primary
        "misplaced" -> MaterialTheme.colorScheme.error
        "completed" -> MaterialTheme.colorScheme.onSurfaceVariant
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Package icon ─────────────────────────────────────────────────
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint               = statusColor,
                modifier           = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(14.dp))

            // ── Package info ─────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = pkg.description,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Created ${pkg.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            // ── Status chip ──────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text      = pkg.status.replaceFirstChar { it.uppercase() },
                    style     = MaterialTheme.typography.labelSmall,
                    color     = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}