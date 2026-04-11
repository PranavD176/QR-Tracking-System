package com.qrtracker.tracko.ui.checkpoint

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*
import com.qrtracker.tracko.ui.theme.ReceivedGreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// ══════════════════════════════════════════════════════════════════════════════
//  Admin Checkpoint — Main Landing Screen
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── Scan Status ──────────────────────────────────────────────────────────────
enum class ScanStatus { RECEIVED, MISPLACED, DUPLICATE }

// ── Scan Result Data ─────────────────────────────────────────────────────────
data class ScanResultData(
    val parcelId: String,
    val status: ScanStatus,
    val timestamp: String,
    val origin: String
)

// ── Recent Scan Item ─────────────────────────────────────────────────────────
data class RecentScan(
    val parcelId: String,
    val timestamp: String,
    val status: ScanStatus
)

// ── Checkpoint Stats ─────────────────────────────────────────────────────────
data class CheckpointStats(
    val total: Int = 124,
    val received: Int = 110,
    val misplaced: Int = 8,
    val duplicate: Int = 6
)

// ── UI State ─────────────────────────────────────────────────────────────────
sealed class CheckpointUiState {
    object Idle : CheckpointUiState()
    object Scanning : CheckpointUiState()
    data class Result(val data: ScanResultData) : CheckpointUiState()
    object Loading : CheckpointUiState()
    data class Error(val message: String) : CheckpointUiState()
    object Offline : CheckpointUiState()
}

// ── Mock Data ────────────────────────────────────────────────────────────────
private val mockRecentScans = listOf(
    RecentScan("PK-9021-RT", "14:45 | Today", ScanStatus.RECEIVED),
    RecentScan("PK-7712-MK", "14:38 | Today", ScanStatus.MISPLACED),
    RecentScan("PK-4456-LL", "14:30 | Today", ScanStatus.DUPLICATE),
    RecentScan("PK-2290-AA", "14:15 | Today", ScanStatus.RECEIVED),
    RecentScan("PK-1105-ZZ", "14:02 | Today", ScanStatus.RECEIVED),
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdminCheckpointScreen(navController: NavController) {

    var uiState by remember {
        mutableStateOf<CheckpointUiState>(
            CheckpointUiState.Result(
                ScanResultData("PK-8829-XL", ScanStatus.RECEIVED, "14:22 | Oct 24", "Hamburg Distribution")
            )
        )
    }
    var manualParcelId by remember { mutableStateOf("") }
    val stats = remember { CheckpointStats() }
    val recentScans = remember { mockRecentScans }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

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
                    NavItem("Packages", Icons.Default.Inventory2, Routes.ADMIN_PACKAGES),
                ),
                currentRoute = Routes.ADMIN_CHECKPOINT,
                onItemClick = { route ->
                    when (route) {
                        Routes.ADMIN_CHECKPOINT -> { /* already here */ }
                        else -> navController.navigate(route) { launchSingleTop = true }
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
            //  Glass Header
            // ══════════════════════════════════════════════════════════════════
            item {
                CheckpointHeader(
                    onProfileClick = {
                        navController.navigate(Routes.ADMIN_PROFILE) { launchSingleTop = true }
                    },
                    onBellClick = {
                        navController.navigate(Routes.ADMIN_ALERTS) { launchSingleTop = true }
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            //  Scanner Section
            // ══════════════════════════════════════════════════════════════════
            item {
                ScannerSection(
                    manualParcelId = manualParcelId,
                    onManualIdChange = { manualParcelId = it },
                    isScanning = uiState is CheckpointUiState.Scanning,
                    hasCameraPermission = cameraPermission.status.isGranted,
                    onStartScan = {
                        if (cameraPermission.status.isGranted) {
                            uiState = CheckpointUiState.Scanning
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                    onSubmitManual = {
                        if (manualParcelId.isNotBlank()) {
                            // TODO: Call ViewModel to process manual ID
                            uiState = CheckpointUiState.Result(
                                ScanResultData(
                                    manualParcelId, ScanStatus.RECEIVED,
                                    "Now", "Manual Entry"
                                )
                            )
                            manualParcelId = ""
                        }
                    },
                    onBarcodeDetected = { rawValue ->
                        uiState = CheckpointUiState.Result(
                            ScanResultData(rawValue, ScanStatus.RECEIVED, "Now", "Camera Scan")
                        )
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            //  Dynamic Result Card
            // ══════════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(
                    visible = uiState is CheckpointUiState.Result,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut()
                ) {
                    val result = (uiState as? CheckpointUiState.Result)?.data
                    if (result != null) {
                        ResultCard(
                            result = result,
                            onConfirm = {
                                uiState = CheckpointUiState.Idle
                                // TODO: Call ViewModel to confirm action
                            },
                            onEscalate = {
                                // TODO: Call ViewModel to escalate
                            },
                            onReroute = {
                                // TODO: Call ViewModel to reroute
                            },
                            onDismiss = {
                                uiState = CheckpointUiState.Idle
                            }
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Loading State
            // ══════════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(visible = uiState is CheckpointUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CoralPrimary)
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Error State
            // ══════════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(visible = uiState is CheckpointUiState.Error) {
                    val errorMsg = (uiState as? CheckpointUiState.Error)?.message ?: ""
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MisplacedRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(errorMsg, style = MaterialTheme.typography.bodyMedium, color = ErrorRed, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        GradientButton(text = "Retry", onClick = { uiState = CheckpointUiState.Idle }, modifier = Modifier.width(160.dp))
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Offline State
            // ══════════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(visible = uiState is CheckpointUiState.Offline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You are offline. Scan will be synced later.", color = OnSurfaceVariant)
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Checkpoint Stats
            // ══════════════════════════════════════════════════════════════════
            item { StatsStrip(stats) }

            // ══════════════════════════════════════════════════════════════════
            //  Recent Scans List
            // ══════════════════════════════════════════════════════════════════
            item {
                Text(
                    "Recent Scans",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PARCEL ID",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = OnSurfaceVariant,
                        modifier = Modifier.weight(3f)
                    )
                    Text(
                        "TIME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = OnSurfaceVariant,
                        modifier = Modifier.weight(2.5f)
                    )
                    Text(
                        "STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = OnSurfaceVariant,
                        modifier = Modifier.weight(2.5f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            items(recentScans) { scan ->
                RecentScanRow(scan)
                Spacer(Modifier.height(1.dp)) // subtle divider
            }
        }
    }
}

// ── Header Component ──────────────────────────────────────────────────────────
@Composable
private fun CheckpointHeader(
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
                    .clickable { onProfileClick() },

                contentAlignment = Alignment.Center
            ) {
                Text(
                    "A",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,

                )

            }
            Spacer(Modifier.width(10.dp))
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
                    "ALEX CHEN (SUPERVISOR) • BERLIN-BER HUB",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    fontSize = 7.sp,
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(ReceivedGreenBg)
                        .border(1.dp, ReceivedGreenBorder, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ReceivedGreen)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Active Shift",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ReceivedGreen
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHighest)
                .clickable { onBellClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Notifications, null, tint = OnSurface, modifier = Modifier.size(24.dp))
        }
    }
}

// ── Scanner Section ──────────────────────────────────────────────────────────
@Composable
private fun ScannerSection(
    manualParcelId: String,
    onManualIdChange: (String) -> Unit,
    isScanning: Boolean,
    hasCameraPermission: Boolean,
    onStartScan: () -> Unit,
    onSubmitManual: () -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Admin Checkpoint",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurface
            )
        }

        Text(
            "Station: SCAN-04-B",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Scanner",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurfaceVariant
            )
            if (isScanning) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ReceivedGreen)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = ReceivedGreen)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Scanner Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            if (isScanning && hasCameraPermission) {
                CameraPreviewComponent(onBarcodeDetected = onBarcodeDetected)

                // Scanning Line Animation
                val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
                val yOffset by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 220f,
                    animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "yOffset"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = yOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White, Color.Transparent)
                            )
                        )
                )
            } else {
                // Placeholder when not scanning
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.1f, targetValue = 0.4f,
                            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                            label = "pulseAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .padding(horizontal = 8.dp)
                                .background(Color.White.copy(alpha = alpha))
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Scanner Inactive",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            // Start Scan Button — overlaid at bottom (only show if not scanning)
            if (!isScanning) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SignatureGradient)
                        .clickable { onStartScan() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Start Scan",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Manual Parcel ID Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = manualParcelId,
                onValueChange = { onManualIdChange(it) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmitManual() }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (manualParcelId.isEmpty()) {
                            Text(
                                "Enter Parcel ID ...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SurfaceContainerHighest)
                    .clickable { onSubmitManual() }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Submit",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewComponent(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    ImageAnalysis.Analyzer { imageProxy ->
                        processImageProxy(imageProxy, onBarcodeDetected)
                    }
                )

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                    ?.let { onBarcodeDetected(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

// ── Dynamic Result Card ──────────────────────────────────────────────────────
@Composable
private fun ResultCard(
    result: ScanResultData,
    onConfirm: () -> Unit,
    onEscalate: () -> Unit,
    onReroute: () -> Unit,
    onDismiss: () -> Unit
) {
    val (gradientBg, borderColor, statusColor, statusText, iconTint) = when (result.status) {
        ScanStatus.RECEIVED -> ResultCardColors(
            Brush.linearGradient(listOf(ReceivedGreenBg, Color.White)),
            ReceivedGreenBorder, ReceivedGreen, "Received at Checkpoint", ReceivedGreen
        )
        ScanStatus.MISPLACED -> ResultCardColors(
            Brush.linearGradient(listOf(MisplacedOrangeBg, Color.White)),
            MisplacedOrangeBorder, MisplacedOrange, "Misplaced — Reroute Required", MisplacedOrange
        )
        ScanStatus.DUPLICATE -> ResultCardColors(
            Brush.linearGradient(listOf(DuplicateGrayBg, Color.White)),
            DuplicateGrayBorder, DuplicateGray, "Duplicate Scan (Internal)", DuplicateGray
        )
    }

    val iconVector = when (result.status) {
        ScanStatus.RECEIVED -> Icons.Filled.CheckCircle
        ScanStatus.MISPLACED -> Icons.Filled.Warning
        ScanStatus.DUPLICATE -> Icons.Filled.ContentCopy
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(ReceivedGreenBg.copy(alpha = 0.5f), Color.White)))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        // Icon + Status Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "SCAN SUCCESSFUL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
                Text(
                    result.parcelId,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Details
        DetailRow("Status", borderColor) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(statusColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = Color.White
                )
            }
        }
        DetailRow("Timestamp", borderColor) {
            Text(result.timestamp, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Origin", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Text(result.origin, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
        }

        Spacer(Modifier.height(16.dp))

        // Action Buttons based on state
        when (result.status) {
            ScanStatus.RECEIVED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xFF1C1917))
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Confirm Received", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = Color.White)
                }
            }
            ScanStatus.MISPLACED -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SignatureGradient)
                            .clickable { onEscalate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Escalate", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(SurfaceContainerHighest)
                            .clickable { onReroute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reroute", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                    }
                }
            }
            ScanStatus.DUPLICATE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(SurfaceContainerHighest)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Dismiss (No Customer Update)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
            }
        }
    }
}

private data class ResultCardColors(
    val gradient: Brush,
    val border: Color,
    val statusColor: Color,
    val statusText: String,
    val iconTint: Color
)

@Composable
private fun DetailRow(label: String, borderColor: Color, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        content()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(borderColor.copy(alpha = 0.4f))
    )
}


// ── Stats Strip ──────────────────────────────────────────────────────────────
@Composable
private fun StatsStrip(stats: CheckpointStats) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox("TOTAL", stats.total.toString(), OnSurfaceVariant, Modifier.weight(1f))
            StatBox("RECEIVED", stats.received.toString(), ReceivedGreen, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox("MISPLACED", stats.misplaced.toString(), MisplacedOrange, Modifier.weight(1f))
            StatBox("DUPLICATE", stats.duplicate.toString(), DuplicateGray, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp
            ),
            color = color
        )
    }
}

// ── Recent Scan Row ──────────────────────────────────────────────────────────
@Composable
private fun RecentScanRow(scan: RecentScan) {
    val (chipBg, chipText, chipBorder) = when (scan.status) {
        ScanStatus.RECEIVED -> Triple(ReceivedGreenBg, ReceivedGreen, ReceivedGreenBorder)
        ScanStatus.MISPLACED -> Triple(MisplacedOrangeBg, MisplacedOrange, MisplacedOrangeBorder)
        ScanStatus.DUPLICATE -> Triple(DuplicateGrayBg, DuplicateGray, DuplicateGrayBorder)
    }
    val statusLabel = when (scan.status) {
        ScanStatus.RECEIVED -> "Received"
        ScanStatus.MISPLACED -> "Misplaced"
        ScanStatus.DUPLICATE -> "Duplicate"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLowest)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            scan.parcelId,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurface,
            modifier = Modifier.weight(3f)
        )
        Text(
            scan.timestamp,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(2.5f)
        )
        Box(modifier = Modifier.weight(2.5f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = chipText
                )
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Icon(
                Icons.Outlined.MoreHoriz,
                contentDescription = "More",
                tint = OnSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
