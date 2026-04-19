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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.CreatePackageState
import com.qrtracker.tracko.viewmodel.AdminUsersState
import androidx.compose.ui.platform.LocalContext

// ══════════════════════════════════════════════════════════════════════════════
//  Create Package — Admin-Only screen
//  Pixel-accurate to: stitch_qr_tracker_app_ui create package design
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── State holders (ViewModel-ready) ──────────────────────────────────────────
data class CreatePackageUiState(
    val companyName     : String   = "",
    val orderId         : String   = "",
    val pickupAddress   : String   = "",
    val customerName    : String   = "",
    val destinationUserId: String? = null,
    val isDropdownExpanded: Boolean = false,
    val deliveryAddress : String   = "",
    val checkpoints     : List<RouteCheckpoint> = listOf(
        RouteCheckpoint("Austin Distribution Center", isOrigin = true),
        RouteCheckpoint("Dallas Sorting Hub"),
    ),
    val isLoading       : Boolean  = false,
    val error           : String?  = null,
    val createdPackageId: String?  = null,
    val createdQrPayload: String?  = null
)

data class RouteCheckpoint(
    val name: String,
    val isOrigin: Boolean = false,
    val isDestination: Boolean = false,
    val isExpanded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageScreen(
    navController: NavController,
    isAdminFlow: Boolean = false
) {
    var uiState           by remember { mutableStateOf(CreatePackageUiState()) }
    val context           = LocalContext.current
    val tokenManager      = remember { TokenManager(context.applicationContext) }
    val packageViewModel  = remember { PackageViewModel(tokenManager) }
    val createState by packageViewModel.createPackageState.collectAsState()
    val usersState by packageViewModel.adminUsersState.collectAsState()
    val focusManager      = LocalFocusManager.current
    val scrollState       = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Fetch users on load ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        if (isAdminFlow) {
            packageViewModel.fetchAdminUsers()
        }
    }

    // ── Observe create package state ───────────────────────────────────
    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreatePackageState.Success -> {
                uiState = uiState.copy(
                    createdPackageId = state.qrPayload,
                    createdQrPayload = "QR_TRACKING:${state.qrPayload}",
                    isLoading = false
                )
            }
            is CreatePackageState.Error -> {
                uiState = uiState.copy(isLoading = false, error = state.message)
                packageViewModel.resetCreatePackageState()
            }
            is CreatePackageState.Loading -> {
                uiState = uiState.copy(isLoading = true)
            }
            else -> {}
        }
    }

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        topBar = {
            // ── Glass Top Bar ────────────────────────────────────────────────
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
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "QR Tracker",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            brush = HorizontalBrandGradient
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable {
                            if (isAdminFlow) {
                                navController.navigate(Routes.ADMIN_ALERTS) { launchSingleTop = true }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                }
            }
        },
        bottomBar = {
            // ── Admin Bottom Nav only ────────────────────────────────────────
            val navItems = listOf(
                NavItem("Home", Icons.Default.Home, Routes.ADMIN_CHECKPOINT),
                NavItem(
                    label = "New",
                    route = Routes.ADMIN_CREATE_PACKAGE,
                    iconContent = { isSelected -> AdminCreateNavIcon(isSelected) }
                ),
                NavItem("Track", Icons.Default.LocalShipping, Routes.ADMIN_PACKAGES),
                NavItem("Settings", Icons.Default.Settings, Routes.ADMIN_PROFILE),
            )

            BottomNavBar(
                items = navItems,
                currentRoute = Routes.ADMIN_CREATE_PACKAGE,
                onItemClick = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Show success state after creation ────────────────────────────
            if (uiState.createdPackageId != null) {
                CreationSuccessCard(
                    packageId  = uiState.createdPackageId!!,
                    qrPayload  = uiState.createdQrPayload ?: "",
                    onGoHome   = {
                        navController.navigate(Routes.ADMIN_CHECKPOINT) {
                            popUpTo(Routes.ADMIN_CHECKPOINT) { inclusive = true }
                        }
                    },
                    onCreateAnother = {
                        uiState = CreatePackageUiState()
                    }
                )
            } else {
                // ══════════════════════════════════════════════════════════════
                //  Editorial Header
                // ══════════════════════════════════════════════════════════════
                Text(
                    text = "New\nPackage",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 52.sp,
                        fontSize = 48.sp
                    ),
                    color = OnSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Create a kinetic pulse for your shipment. Map out the journey and generate a unique tracking ID.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  SOURCE DETAILS
                // ══════════════════════════════════════════════════════════════
                EditorialLabel(
                    text = "SOURCE DETAILS",
                    color = CoralPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Company Name
                    FormField(
                        label = "COMPANY NAME",
                        value = uiState.companyName,
                        placeholder = "Pulse Logistics Co.",
                        onValueChange = { uiState = uiState.copy(companyName = it) }
                    )
                    // Order ID
                    FormField(
                        label = "ORDER ID",
                        value = uiState.orderId,
                        placeholder = "ORD-7742-XP-2624",
                        onValueChange = { uiState = uiState.copy(orderId = it) }
                    )
                    // Pickup Address
                    FormField(
                        label = "PICKUP ADDRESS",
                        value = uiState.pickupAddress,
                        placeholder = "122 Industrial Way, Suite 400, Austin, TX 78701",
                        onValueChange = { uiState = uiState.copy(pickupAddress = it) },
                        singleLine = false
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  DESTINATION DETAILS
                // ══════════════════════════════════════════════════════════════
                EditorialLabel(
                    text = "DESTINATION DETAILS",
                    color = CoralPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DESTINATION USER DROPDOWN
                    ExposedDropdownMenuBox(
                        expanded = uiState.isDropdownExpanded,
                        onExpandedChange = { uiState = uiState.copy(isDropdownExpanded = it) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.customerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ASSIGN TO USER", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("Select destination user") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralPrimary,
                                focusedLabelColor = CoralPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = uiState.isDropdownExpanded,
                            onDismissRequest = { uiState = uiState.copy(isDropdownExpanded = false) }
                        ) {
                            if (usersState is AdminUsersState.Loading) {
                                DropdownMenuItem(
                                    text = { Text("Loading users...") },
                                    onClick = { }
                                )
                            } else if (usersState is AdminUsersState.Success) {
                                val users = (usersState as AdminUsersState.Success).users
                                if (users.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No users found") },
                                        onClick = { }
                                    )
                                } else {
                                    users.forEach { user ->
                                        DropdownMenuItem(
                                            text = { Text("${user.full_name} (${user.email})") },
                                            onClick = {
                                                uiState = uiState.copy(
                                                    customerName = user.full_name,
                                                    destinationUserId = user.user_id,
                                                    isDropdownExpanded = false
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Failed to load users") },
                                    onClick = { uiState = uiState.copy(isDropdownExpanded = false) }
                                )
                            }
                        }
                    }

                    FormField(
                        label = "DELIVERY ADDRESS",
                        value = uiState.deliveryAddress,
                        placeholder = "Street, City, State, ZIP",
                        onValueChange = { uiState = uiState.copy(deliveryAddress = it) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  ROUTE BUILDER
                // ══════════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorialLabel(text = "ROUTE BUILDER", color = CoralPrimary)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PrimaryContainer.copy(alpha = 0.15f))
                            .clickable {
                                val newList = uiState.checkpoints.toMutableList()
                                val insertIdx = (newList.size - 1).coerceAtLeast(1)
                                newList.add(
                                    insertIdx,
                                    RouteCheckpoint("New Checkpoint")
                                )
                                uiState = uiState.copy(checkpoints = newList)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ADD CHECKPOINT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = CoralPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Route timeline
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.checkpoints.forEachIndexed { index, checkpoint ->
                        RouteCheckpointItem(
                            checkpoint = checkpoint,
                            index = index,
                            total = uiState.checkpoints.size,
                            onDelete = {
                                if (!checkpoint.isOrigin) {
                                    val newList = uiState.checkpoints.toMutableList()
                                    newList.removeAt(index)
                                    uiState = uiState.copy(checkpoints = newList)
                                }
                            }
                        )
                    }

                    // Destination (pending)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(OnSurfaceVariant.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "DESTINATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 9.sp
                                ),
                                color = OnSurfaceVariant
                            )
                            Text(
                                if (uiState.deliveryAddress.isNotBlank()) uiState.deliveryAddress
                                else "Pending address input...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.deliveryAddress.isNotBlank()) OnSurface
                                else OnSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.DragHandle, null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  Create Package & ID Button
                // ══════════════════════════════════════════════════════════════
                GradientButton(
                    text = "Create Package & ID",
                    icon = Icons.Default.QrCode2,
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        when {
                            uiState.companyName.isBlank() ->
                                uiState = uiState.copy(error = "Company name is required")
                            uiState.pickupAddress.isBlank() ->
                                uiState = uiState.copy(error = "Pickup address is required")
                            else -> {
                                // Build description from form fields
                                val desc = "${uiState.companyName} - ${uiState.orderId.ifBlank { "Package" }}"
                                packageViewModel.createPackage(
                                    description = desc,
                                    destinationUserId = uiState.destinationUserId,
                                    destinationAddress = uiState.deliveryAddress.takeIf { it.isNotBlank() },
                                    routeCheckpoints = uiState.checkpoints
                                )
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(100.dp)) // Bottom nav clearance
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Form Field — Minimal labeled input matching the Stitch design
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun FormField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                fontSize = 10.sp
            ),
            color = OnSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = OnSurface,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceContainerHighest)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Route Checkpoint Item — Timeline row with dot, line, name, and controls
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun RouteCheckpointItem(
    checkpoint: RouteCheckpoint,
    index: Int,
    total: Int,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline: dot + vertical line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (checkpoint.isOrigin) CoralPrimary
                        else SurfaceContainerHighest
                    )
                    .then(
                        if (!checkpoint.isOrigin) Modifier.border(2.dp, CoralPrimary.copy(alpha = 0.4f), CircleShape)
                        else Modifier
                    )
            )
            if (index < total - 1 || true) { // always draw line to next item
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(52.dp)
                        .background(SurfaceContainerHighest)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            if (checkpoint.isOrigin) {
                Text(
                    "ORIGIN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    ),
                    color = OnSurfaceVariant
                )
            } else {
                Text(
                    "CHECKPOINT ${index}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    checkpoint.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (checkpoint.isOrigin) {
                        Icon(
                            Icons.Default.Lock,
                            null,
                            tint = OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.ExpandMore,
                            null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            Icons.Default.ExpandLess,
                            null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ErrorContainer.copy(alpha = 0.15f))
                                .clickable { onDelete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Success card shown after package creation ────────────────────────────────
@Composable
private fun CreationSuccessCard(
    packageId      : String,
    qrPayload      : String,
    onGoHome       : () -> Unit,
    onCreateAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(EmeraldActiveBg)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(EmeraldActive.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = EmeraldActive,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Package Created!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = ValidGreen
        )
        Spacer(Modifier.height(20.dp))

        // QR Code placeholder
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.QrCode2,
                contentDescription = null,
                tint = ValidGreen.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        // Package ID
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .padding(14.dp)
        ) {
            Column {
                EditorialLabel(text = "Tracking ID", color = ValidGreen.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = qrPayload,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ValidGreen
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryActionButton(
                text = "Share",
                icon = Icons.Outlined.Share,
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
            GradientButton(
                text = "Home",
                onClick = onGoHome,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        SecondaryActionButton(
            text = "Create Another",
            onClick = onCreateAnother
        )
    }
}
