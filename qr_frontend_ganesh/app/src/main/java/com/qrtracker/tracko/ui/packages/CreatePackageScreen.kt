package com.qrtracker.tracko.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.qrtracker.tracko.viewmodel.CreatePackageState
import com.qrtracker.tracko.viewmodel.PackageViewModel

// ── State holder ─────────────────────────────────────────────────────────────
data class CreatePackageUiState(
    val description     : String  = "",
    val isLoading       : Boolean = false,
    val error           : String? = null,
    val createdPackageId: String? = null,   // non-null = success
    val createdQrPayload: String? = null    // QR string to display after creation
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageScreen(navController: NavController) {

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val viewModel = remember { PackageViewModel(tokenManager) }
    val createPackageState by viewModel.createPackageState.collectAsState()

    var uiState           by remember { mutableStateOf(CreatePackageUiState()) }
    val focusManager      = LocalFocusManager.current
    val scrollState       = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val charLimit         = 200

    LaunchedEffect(createPackageState) {
        when (val state = createPackageState) {
            is CreatePackageState.Loading -> {
                uiState = uiState.copy(isLoading = true)
            }
            is CreatePackageState.Success -> {
                val payload = state.qrPayload.ifBlank {
                    "QR_TRACKING:unknown-${System.currentTimeMillis()}"
                }
                val packageId = payload.removePrefix("QR_TRACKING:").ifBlank {
                    "unknown-${System.currentTimeMillis()}"
                }
                uiState = uiState.copy(
                    isLoading = false,
                    createdPackageId = packageId,
                    createdQrPayload = payload
                )
                viewModel.resetCreatePackageState()
            }
            is CreatePackageState.Error -> {
                uiState = uiState.copy(isLoading = false, error = state.message)
                viewModel.resetCreatePackageState()
            }
            CreatePackageState.Idle -> {
                if (uiState.isLoading) {
                    uiState = uiState.copy(isLoading = false)
                }
            }
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
                        navController.navigate(Routes.PACKAGE_LIST) {
                            popUpTo(Routes.PACKAGE_LIST) { inclusive = true }
                        }
                    },
                    onCreateAnother = {
                        uiState = CreatePackageUiState()
                        viewModel.resetCreatePackageState()
                    }
                )
            } else {

                // ══════════════════════════════════════════════════════════════
                //  Editorial Header
                // ══════════════════════════════════════════════════════════════
                Text(
                    text = "New\nPackage.",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 48.sp
                    ),
                    color = OnSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Register a new package and generate a unique QR tracking code.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(28.dp))

                // ══════════════════════════════════════════════════════════════
                //  Decorative Card
                // ══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SignatureGradient)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "READY FOR TRANSIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Fill in the details below",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ══════════════════════════════════════════════════════════════
                //  Description Field
                // ══════════════════════════════════════════════════════════════
                EditorialTextField(
                    value = uiState.description,
                    onValueChange = {
                        if (it.length <= charLimit)
                            uiState = uiState.copy(description = it)
                    },
                    label = "Package Description",
                    placeholder = "e.g. Physics textbook — blue cover",
                    leadingIcon = Icons.Default.Inventory2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = false,
                    isError = uiState.error != null
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${uiState.description.length} / $charLimit",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.description.length >= charLimit) ErrorRed else OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(Modifier.height(24.dp))

                // ── Create button ────────────────────────────────────────────
                GradientButton(
                    text = "Create Package",
                    icon = Icons.Default.Add,
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        when {
                            uiState.description.isBlank() ->
                                uiState = uiState.copy(
                                    error = "Package description cannot be empty"
                                )
                            uiState.description.trim().length < 3 ->
                                uiState = uiState.copy(
                                    error = "Description must be at least 3 characters"
                                )
                            else -> {
                                viewModel.createPackage(uiState.description.trim())
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(100.dp)) // Bottom nav clearance
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
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "ID: $packageId",
                    style = MaterialTheme.typography.labelSmall,
                    color = ValidGreen.copy(alpha = 0.7f)
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
