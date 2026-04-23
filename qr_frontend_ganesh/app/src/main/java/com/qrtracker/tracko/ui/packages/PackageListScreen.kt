package com.qrtracker.tracko.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Mouse
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.AlertViewModel
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.PackageListState
import androidx.compose.ui.platform.LocalLifecycleOwner

// ── Mock data class — kept for UI, now populated from API ────────────────────
data class PackageItem(
    val packageId   : String,
    val description : String,
    val trackingId  : String = "",
    val shortId     : String = "",
    val status      : String,
    val createdAt   : String,
    val progress    : Float = 0f,
    val checkpointCount: Int = 0,       // total route checkpoints
    val lastCheckpoint: String = "",
    val source: String = "",
    val destination: String = "",
    val icon        : ImageVector = Icons.Outlined.Laptop
)

// ── State holder ─────────────────────────────────────────────────────────
data class PackageListUiState(
    val packages     : List<PackageItem> = emptyList(),
    val userName     : String            = "User",
    val searchQuery  : String            = "",
    val showAll      : Boolean           = false,
    val inTransit    : Int               = 0,
    val delivered    : Int               = 0,
    val isLoading    : Boolean           = false,
    val isRefreshing : Boolean           = false,
    val error        : String?           = null,
    val showLogout   : Boolean           = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageListScreen(
    navController: NavController,
    startWithAll: Boolean = false,
    alertViewModel: AlertViewModel,
) {

    var uiState           by remember { mutableStateOf(PackageListUiState(showAll = startWithAll)) }
    val context           = LocalContext.current
    val lifecycleOwner    = LocalLifecycleOwner.current
    val tokenManager      = remember { TokenManager(context.applicationContext) }
    val packageViewModel  = remember { PackageViewModel(tokenManager) }
    val pkgListState by packageViewModel.packageListState.collectAsState()
    val unreadCount by alertViewModel.unreadCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Load packages from API on screen entry ────────────────────────────
    LaunchedEffect(Unit) {
        val name = tokenManager.getFullName() ?: "User"
        uiState = uiState.copy(userName = name)
        packageViewModel.fetchPackages()
        alertViewModel.fetchAlerts("sent")
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                alertViewModel.fetchAlerts("sent")
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ── Observe package list state ──────────────────────────────────────
    LaunchedEffect(pkgListState) {
        when (val state = pkgListState) {
            is PackageListState.Loading -> {
                uiState = uiState.copy(isLoading = true)
            }
            is PackageListState.Success -> {
                val items = state.packages.map { pkg ->
                    val fullId = pkg.qr_payload ?: pkg.package_id
                    val displayId = if (fullId.length > 16) fullId.take(8) + "..." + fullId.takeLast(4) else fullId
                    PackageItem(
                        packageId = pkg.package_id,
                        description = pkg.description,
                        trackingId = fullId,
                        shortId = displayId,
                        status = pkg.status,
                        createdAt = pkg.created_at ?: "",
                        // Dynamic progress based on actual scanned checkpoints:
                        // Route has: sender → checkpoint_1 → ... → checkpoint_n → receiver
                        // Total segments = checkpoints + 2 (sender + receiver)
                        // Progress = (scanned_checkpoints + 1) / (total_checkpoints + 2)
                        // +1 because sender already has the package
                        progress = when (pkg.status) {
                            "delivered" -> 1.0f
                            "pending_acceptance" -> 0.05f
                            "rejected" -> 0f
                            else -> {
                                val totalCheckpoints = pkg.route_checkpoints?.size ?: 0
                                val scanned = pkg.scanned_checkpoint_count
                                if (totalCheckpoints == 0) 0.5f
                                else ((scanned + 1).toFloat() / (totalCheckpoints + 2).toFloat()).coerceIn(0.05f, 0.95f)
                            }
                        },
                        checkpointCount = pkg.route_checkpoints?.size ?: 0,
                        lastCheckpoint = pkg.description,
                        source = pkg.sender_name ?: "Unknown",
                        destination = pkg.receiver_name ?: "Unknown"
                    )
                }
                val activeCount = items.count { it.status == "in_transit" }
                val completedCount = items.count { it.status == "delivered" }
                uiState = uiState.copy(
                    packages = items,
                    inTransit = activeCount,
                    delivered = completedCount,
                    isLoading = false,
                    error = null
                )
            }
            is PackageListState.Error -> {
                uiState = uiState.copy(isLoading = false, error = state.message)
            }
            else -> {}
        }
    }

    val displayedPackages = remember(uiState.packages, uiState.searchQuery, uiState.showAll) {
        uiState.packages
            .filter {
                if (uiState.showAll) true else it.status.equals("in_transit", ignoreCase = true)
            }
            .filter {
                val query = uiState.searchQuery.trim()
                if (query.isEmpty()) true
                else it.trackingId.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            }
    }

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
                    Text("Logout", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    uiState = uiState.copy(showLogout = false)
                }) { Text("Cancel") }
            }
        )
    }

    // Determine which bottom nav item is "current" based on showAll state
    val currentNavRoute = if (uiState.showAll) Routes.PACKAGE_LIST else Routes.HOME

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CREATE_PACKAGE) { launchSingleTop = true } },
                containerColor = CoralPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Parcel")
            }
        },
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.HOME),
                    NavItem("Scan", Icons.Default.QrCodeScanner, Routes.SCANNER),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.PACKAGE_LIST),
                    NavItem("Alerts", Icons.Default.Notifications, Routes.ALERTS),
                ),
                currentRoute = currentNavRoute,
                onItemClick = { route ->
                    when (route) {
                        Routes.HOME -> {
                            if (uiState.showAll) {
                                // Switch to active-only home view
                                uiState = uiState.copy(showAll = false)
                            }
                            // Already on this screen
                        }
                        Routes.PACKAGE_LIST -> {
                            if (!uiState.showAll) {
                                // Switch to full package list
                                uiState = uiState.copy(showAll = true)
                            }
                            // Already on this screen
                        }
                        else -> {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ══════════════════════════════════════════════════════════════════
            //  Glass Top Bar with Avatar
            // ══════════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassWhite)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                                .clickable { 
                                    navController.navigate(Routes.USER_PROFILE) { launchSingleTop = true }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.userName.first().toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "WELCOME BACK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "Hello, ${uiState.userName}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    brush = HorizontalBrandGradient
                                )
                            )
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Routes.ALERTS) { launchSingleTop = true } },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer)
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = OnSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Search Bar
            // ══════════════════════════════════════════════════════════════════
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x05000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = {
                                uiState = uiState.copy(searchQuery = it)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions.Default,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Track package, enter ID...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Active Pulse Section Header
            // ══════════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Active Pulse",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = if (uiState.showAll) "View Active" else "View All",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CoralPrimary,
                        modifier = Modifier.clickable {
                            val newShowAll = !uiState.showAll
                            uiState = uiState.copy(showAll = newShowAll)
                            // When toggling, this effectively switches between Home/Packages view
                        }
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Package Cards
            // ══════════════════════════════════════════════════════════════════
            items(displayedPackages, key = { it.packageId }) { pkg ->
                PackageCard(
                    pkg = pkg,
                    onClick = {
                        navController.navigate(Routes.packageDetail(pkg.packageId))
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }


            // ══════════════════════════════════════════════════════════════════
            //  Stats Bento Grid
            // ══════════════════════════════════════════════════════════════════
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.LocalShipping,
                        iconColor = CoralPrimary,
                        count = uiState.inTransit.toString(),
                        label = "In Transit",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.Inventory2,
                        iconColor = CoralSecondary,
                        count = uiState.delivered.toString(),
                        label = "Delivered",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ── Package Card ─────────────────────────────────────────────────────────────
@Composable
private fun PackageCard(
    pkg: PackageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = pkg.status == "delivered"
    val isMisplaced = pkg.status == "misplaced"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCompleted)
                    Modifier
                        .border(
                            2.dp,
                            SurfaceContainerHighest,
                            RoundedCornerShape(16.dp)
                        )
                        .alpha(0.6f)
                else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCompleted) SurfaceContainerLow.copy(alpha = 0.5f)
                else SurfaceContainerLowest
            )
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column {
            // Title + Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pkg.description,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ID: ${pkg.shortId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                StatusChip(status = pkg.status)
            }

            // Progress bar
            if (!isCompleted) {
                Spacer(Modifier.height(16.dp))
                TrackingProgressBar(
                    progress = pkg.progress,
                    isError = isMisplaced
                )
            }

            // Checkpoint info
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCompleted) SurfaceContainerHighest
                            else SurfaceContainerLow
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        pkg.icon,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isCompleted) "Delivered on" else if (isMisplaced) "Required Action" else "Last checkpoint",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = pkg.lastCheckpoint,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isMisplaced) StatusRedText else OnSurface
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${pkg.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "To: ${pkg.destination}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Stat Card ────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = OnSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = OnSurfaceVariant
        )
    }
}
