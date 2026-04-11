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
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager

// ── Mock data class — Member 2 replaces with real model ──────────────────────
data class PackageItem(
    val packageId   : String,
    val description : String,
    val trackingId  : String = "",
    val status      : String,
    val createdAt   : String,
    val progress    : Float = 0f,
    val lastCheckpoint: String = "",
    val icon        : ImageVector = Icons.Outlined.Laptop
)

// ── Mock list — Member 2 replaces with ViewModel state ───────────────────────
private val mockPackages = listOf(
    PackageItem(
        "uuid-001", "MacBook Pro 16\"", "QRT-8829-XL",
        "active", "2026-04-01", 0.72f,
        "Distribution Center, SF", Icons.Outlined.Laptop
    ),
    PackageItem(
        "uuid-002", "Leather Briefcase", "QRT-4410-BK",
        "misplaced", "2026-04-02", 0.45f,
        "Contact Support", Icons.Outlined.Work
    ),
    PackageItem(
        "uuid-003", "Ergonomic Mouse", "QRT-1192-MS",
        "completed", "2026-03-28", 1f,
        "Oct 24, 2023", Icons.Outlined.Mouse
    ),
)

// ── State holder ─────────────────────────────────────────────────────────────
data class PackageListUiState(
    val packages     : List<PackageItem> = mockPackages,
    val userName     : String            = "Ganesh",
    val searchQuery  : String            = "",
    val showAll      : Boolean           = false,
    val inTransit    : Int               = 12,
    val delivered    : Int               = 48,
    val isLoading    : Boolean           = false,
    val isRefreshing : Boolean           = false,
    val error        : String?           = null,
    val showLogout   : Boolean           = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageListScreen(navController: NavController) {

    var uiState           by remember { mutableStateOf(PackageListUiState()) }
    var showProfileMenu   by remember { mutableStateOf(false) }
    val context           = LocalContext.current
    val tokenManager      = remember { TokenManager(context.applicationContext) }
    val snackbarHostState = remember { SnackbarHostState() }

    val displayedPackages = remember(uiState.packages, uiState.searchQuery, uiState.showAll) {
        uiState.packages
            .filter {
                if (uiState.showAll) true else it.status.equals("active", ignoreCase = true)
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
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
                    if (route != Routes.PACKAGE_LIST) {
                        navController.navigate(route) {
                            launchSingleTop = true
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
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariant)
                                    .clickable { showProfileMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.userName.first().toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = OnSurfaceVariant
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = {
                                        showProfileMenu = false
                                        tokenManager.clearAll()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
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
                            uiState = uiState.copy(showAll = !uiState.showAll)
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
    val isCompleted = pkg.status == "completed"
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
                        text = "ID: ${pkg.trackingId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
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
