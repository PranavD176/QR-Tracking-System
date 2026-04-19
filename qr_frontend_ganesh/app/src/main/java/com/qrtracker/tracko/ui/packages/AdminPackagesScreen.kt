package com.qrtracker.tracko.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.PackageListState
import androidx.compose.ui.platform.LocalContext
import com.qrtracker.tracko.ui.theme.AdminCreateNavIcon
import com.qrtracker.tracko.ui.theme.BottomNavBar
import com.qrtracker.tracko.ui.theme.GlassWhite
import com.qrtracker.tracko.ui.theme.NavItem
import com.qrtracker.tracko.ui.theme.OnSurface
import com.qrtracker.tracko.ui.theme.OnSurfaceVariant
import com.qrtracker.tracko.ui.theme.ReceivedGreen
import com.qrtracker.tracko.ui.theme.ReceivedGreenBg
import com.qrtracker.tracko.ui.theme.ReceivedGreenBorder
import com.qrtracker.tracko.ui.theme.Surface
import com.qrtracker.tracko.ui.theme.SurfaceContainerHigh
import com.qrtracker.tracko.ui.theme.SurfaceContainerLowest
import com.qrtracker.tracko.ui.theme.HorizontalBrandGradient
import com.qrtracker.tracko.ui.theme.MisplacedOrange
import com.qrtracker.tracko.ui.theme.MisplacedOrangeBg
import com.qrtracker.tracko.ui.theme.MisplacedOrangeBorder

data class AdminPendingPackage(
    val parcelId: String,
    val station: String,
    val pendingType: String,
    val eta: String
)

@Composable
fun AdminPackagesScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val packageViewModel = remember { PackageViewModel(tokenManager) }
    val pkgListState by packageViewModel.packageListState.collectAsState()
    var pendingPackages by remember { mutableStateOf<List<AdminPendingPackage>>(emptyList()) }

    // Load packages from API
    LaunchedEffect(Unit) {
        packageViewModel.fetchPackages()
    }

    // Map API response to UI model
    LaunchedEffect(pkgListState) {
        if (pkgListState is PackageListState.Success) {
            pendingPackages = (pkgListState as PackageListState.Success).packages.map { pkg ->
                AdminPendingPackage(
                    parcelId = pkg.qr_payload ?: pkg.package_id.take(12),
                    station = "SCAN-04-B",
                    pendingType = if (pkg.status == "active") "Not Scanned" else "Left To Scan",
                    eta = pkg.created_at?.take(10) ?: "—"
                )
            }
        }
    }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    NavItem("Home", Icons.Default.Home, Routes.ADMIN_CHECKPOINT),
                    NavItem(
                        label = "Create",
                        route = Routes.ADMIN_CREATE_PACKAGE,
                        iconContent = { isSelected -> AdminCreateNavIcon(isSelected) }
                    ),
                    NavItem("Packages", Icons.Default.Inventory2, Routes.ADMIN_PACKAGES)
                ),
                currentRoute = Routes.ADMIN_PACKAGES,
                onItemClick = { route ->
                    if (route != Routes.ADMIN_PACKAGES) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassWhite)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SurfaceContainerHigh, CircleShape)
                                .clickable { navController.navigate(Routes.ADMIN_PROFILE) { launchSingleTop = true } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = OnSurfaceVariant, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Warehouse Control",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    brush = HorizontalBrandGradient
                                )
                            )
                            Text(
                                "Packages Pending Scan",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(ReceivedGreenBg, RoundedCornerShape(9999.dp))
                                .border(1.dp, ReceivedGreenBorder, RoundedCornerShape(9999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${pendingPackages.size} Pending",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = ReceivedGreen
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .clickable { navController.navigate(Routes.ADMIN_ALERTS) { launchSingleTop = true } },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = OnSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        "Left / Not Scanned Packages",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Backend-ready list: replace mock data with API packages assigned to this station.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            items(pendingPackages) { pkg ->
                PendingPackageCard(item = pkg)
            }
        }
    }
}

@Composable
private fun PendingPackageCard(item: AdminPendingPackage) {
    val isNotScanned = item.pendingType.equals("Not Scanned", ignoreCase = true)
    val chipBg = if (isNotScanned) MisplacedOrangeBg else SurfaceContainerHigh
    val chipBorder = if (isNotScanned) MisplacedOrangeBorder else ReceivedGreenBorder
    val chipText = if (isNotScanned) MisplacedOrange else OnSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .background(SurfaceContainerLowest, RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MisplacedOrangeBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MisplacedOrange,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.parcelId,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Station ${item.station} • ${item.eta}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .background(chipBg, RoundedCornerShape(9999.dp))
                .border(1.dp, chipBorder, RoundedCornerShape(9999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                item.pendingType,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = chipText
            )
        }
    }
}

