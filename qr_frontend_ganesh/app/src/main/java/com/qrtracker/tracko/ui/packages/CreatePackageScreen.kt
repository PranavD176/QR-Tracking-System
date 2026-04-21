package com.qrtracker.tracko.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.utils.TokenManager
import com.qrtracker.tracko.viewmodel.PackageViewModel
import com.qrtracker.tracko.viewmodel.CreatePackageState
import com.qrtracker.tracko.viewmodel.UsersState

// ══════════════════════════════════════════════════════════════════════════════
//  Create Package — P2P: any user can send a parcel to any other user
// ══════════════════════════════════════════════════════════════════════════════

data class CreatePackageUiState(
    val description           : String  = "",
    val receiverName          : String  = "",
    val receiverId            : String? = null,
    val isReceiverDropdown    : Boolean = false,
    val intermediates         : List<IntermediateUser> = emptyList(),
    val isIntDropdown         : Boolean = false,
    val isLoading             : Boolean = false,
    val error                 : String? = null,
    val createdPackageId      : String? = null,
    val createdQrPayload      : String? = null
)

data class IntermediateUser(
    val userId: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageScreen(navController: NavController) {
    var uiState           by remember { mutableStateOf(CreatePackageUiState()) }
    val context           = LocalContext.current
    val tokenManager      = remember { TokenManager(context.applicationContext) }
    val packageViewModel  = remember { PackageViewModel(tokenManager) }
    val createState by packageViewModel.createPackageState.collectAsState()
    val usersState by packageViewModel.usersState.collectAsState()
    val focusManager      = LocalFocusManager.current
    val scrollState       = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch users on load
    LaunchedEffect(Unit) {
        packageViewModel.fetchUsers()
    }

    // Observe create package state
    LaunchedEffect(createState) {
        when (val state = createState) {
            is CreatePackageState.Success -> {
                uiState = uiState.copy(
                    createdPackageId = state.packageId,
                    createdQrPayload = state.qrPayload,
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

    // Show error in snackbar
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
                        "New Parcel",
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
                            navController.navigate(Routes.ALERTS) { launchSingleTop = true }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, null, tint = OnSurface, modifier = Modifier.size(22.dp))
                }
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
                currentRoute = Routes.CREATE_PACKAGE,
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

            // Show success state after creation
            if (uiState.createdPackageId != null) {
                CreationSuccessCard(
                    packageId  = uiState.createdPackageId!!,
                    qrPayload  = uiState.createdQrPayload ?: "",
                    onGoHome   = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onCreateAnother = {
                        uiState = CreatePackageUiState()
                        packageViewModel.resetCreatePackageState()
                    }
                )
            } else {
                // ══════════════════════════════════════════════════════════════
                //  Editorial Header
                // ══════════════════════════════════════════════════════════════
                Text(
                    text = "Send\nParcel",
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
                    text = "Send a parcel to anyone. Pick a receiver, add intermediates, and generate a unique tracking QR.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  PARCEL DETAILS
                // ══════════════════════════════════════════════════════════════
                EditorialLabel(
                    text = "PARCEL DETAILS",
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
                    FormField(
                        label = "DESCRIPTION",
                        value = uiState.description,
                        placeholder = "e.g. Laptop, Documents, Gift Box",
                        onValueChange = { uiState = uiState.copy(description = it) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  RECEIVER
                // ══════════════════════════════════════════════════════════════
                EditorialLabel(
                    text = "SEND TO",
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
                        .padding(20.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = uiState.isReceiverDropdown,
                        onExpandedChange = { uiState = uiState.copy(isReceiverDropdown = it) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.receiverName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("RECEIVER", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("Select who to send to") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isReceiverDropdown) },
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
                            expanded = uiState.isReceiverDropdown,
                            onDismissRequest = { uiState = uiState.copy(isReceiverDropdown = false) }
                        ) {
                            if (usersState is UsersState.Loading) {
                                DropdownMenuItem(
                                    text = { Text("Loading users...") },
                                    onClick = { }
                                )
                            } else if (usersState is UsersState.Success) {
                                val users = (usersState as UsersState.Success).users
                                if (users.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No users found") },
                                        onClick = { }
                                    )
                                } else {
                                    users.forEach { user ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        user.full_name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    Text(
                                                        user.email,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = OnSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                uiState = uiState.copy(
                                                    receiverName = user.full_name,
                                                    receiverId = user.user_id,
                                                    isReceiverDropdown = false
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Failed to load users") },
                                    onClick = { uiState = uiState.copy(isReceiverDropdown = false) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  INTERMEDIATES (Route Checkpoints)
                // ══════════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorialLabel(text = "INTERMEDIATES", color = CoralPrimary)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PrimaryContainer.copy(alpha = 0.15f))
                            .clickable {
                                uiState = uiState.copy(isIntDropdown = true)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline, null,
                            tint = CoralPrimary, modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ADD PERSON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = CoralPrimary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Optional. People who'll handle this parcel before the receiver.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                // Add intermediate dropdown
                if (uiState.isIntDropdown) {
                    ExposedDropdownMenuBox(
                        expanded = uiState.isIntDropdown,
                        onExpandedChange = { uiState = uiState.copy(isIntDropdown = it) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select person") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isIntDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = uiState.isIntDropdown,
                            onDismissRequest = { uiState = uiState.copy(isIntDropdown = false) }
                        ) {
                            if (usersState is UsersState.Success) {
                                val users = (usersState as UsersState.Success).users
                                    .filter { u -> u.user_id != uiState.receiverId && uiState.intermediates.none { it.userId == u.user_id } }
                                users.forEach { user ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    user.full_name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                                Text(
                                                    user.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            uiState = uiState.copy(
                                                intermediates = uiState.intermediates + IntermediateUser(user.user_id, user.full_name),
                                                isIntDropdown = false
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Show added intermediates as chips
                uiState.intermediates.forEachIndexed { index, intermediate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, SurfaceContainerHighest, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Order indicator
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CoralPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CoralPrimary
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                intermediate.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ErrorContainer.copy(alpha = 0.15f))
                                .clickable {
                                    uiState = uiState.copy(
                                        intermediates = uiState.intermediates.toMutableList().also { it.removeAt(index) }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close, null,
                                tint = ErrorRed, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ══════════════════════════════════════════════════════════════
                //  Create Parcel Button
                // ══════════════════════════════════════════════════════════════
                GradientButton(
                    text = "Create & Generate QR",
                    icon = Icons.Default.QrCode2,
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        when {
                            uiState.description.isBlank() ->
                                uiState = uiState.copy(error = "Description is required")
                            uiState.receiverId == null ->
                                uiState = uiState.copy(error = "Please select a receiver")
                            else -> {
                                packageViewModel.createPackage(
                                    description = uiState.description,
                                    receiverId = uiState.receiverId!!,
                                    routeCheckpoints = uiState.intermediates.map { it.userId }.takeIf { it.isNotEmpty() }
                                )
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Form Field — Minimal labeled input
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
        BasicTextField(
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
            text = "Parcel Created!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = ValidGreen
        )
        Spacer(Modifier.height(20.dp))

        // QR Code
        val qrBitmap = remember(qrPayload) {
            com.qrtracker.tracko.utils.QRCodeGenerator.generate(qrPayload, 512, 512)
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.QrCode2,
                    contentDescription = null,
                    tint = ValidGreen.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Tracking ID — truncated for readability
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
                    text = packageId.take(8) + "..." + packageId.takeLast(4),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ValidGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            SecondaryActionButton(
                text = "Download",
                icon = Icons.Outlined.Download,
                onClick = {
                    qrBitmap?.let {
                        val success = com.qrtracker.tracko.utils.QRCodeGenerator.saveToGallery(context, it, "QR_$qrPayload")
                        if (success) {
                            android.widget.Toast.makeText(context, "QR Code saved to Gallery", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to save QR Code", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = "Share",
                icon = Icons.Outlined.Share,
                onClick = {
                    val sendIntent: android.content.Intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Track your parcel: $qrPayload")
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Tracking ID")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = "Home",
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        SecondaryActionButton(
            text = "Create Another",
            onClick = onCreateAnother
        )
    }
}
