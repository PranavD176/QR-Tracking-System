package com.ganesh.qrtracker.ui.scan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.*
import com.ganesh.qrtracker.utils.QRParser
import com.ganesh.qrtracker.utils.TokenManager
import com.ganesh.qrtracker.viewmodel.ScanState
import com.ganesh.qrtracker.viewmodel.ScanViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// ── State holder ─────────────────────────────────────────────────────────────
data class ScanUiState(
    val locationInput   : String  = "",
    val isTorchOn       : Boolean = false,
    val isProcessing    : Boolean = false,
    val error           : String? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavController) {

    var uiState           by remember { mutableStateOf(ScanUiState()) }
    var lastScanTime      by remember { mutableLongStateOf(0L) }
    val cameraPermission  = rememberPermissionState(Manifest.permission.CAMERA)
    val context           = LocalContext.current
    val tokenManager      = remember { TokenManager(context.applicationContext) }
    val viewModel         = remember { ScanViewModel(tokenManager) }
    val scanState         by viewModel.scanState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scanState) {
        when (val state = scanState) {
            is ScanState.Loading -> {
                uiState = uiState.copy(isProcessing = true)
            }
            is ScanState.Success -> {
                val response = state.scanResponse
                navController.navigate(
                    Routes.scanResult(
                        result = response.result,
                        packageDesc = response.package_description,
                        ownerName = response.owner_name,
                        alertSent = response.alert_sent
                    )
                )
                uiState = uiState.copy(isProcessing = false)
                viewModel.resetScanState()
            }
            is ScanState.Error -> {
                uiState = uiState.copy(isProcessing = false, error = state.message)
                viewModel.resetScanState()
            }
            ScanState.Idle -> {
                if (uiState.isProcessing) {
                    uiState = uiState.copy(isProcessing = false)
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
        containerColor = Color.Black
    ) { padding ->

        when {

            // ── Permission granted → show camera ─────────────────────────────
            cameraPermission.status.isGranted -> {
                Box(modifier = Modifier.fillMaxSize()) {

                    // ── CameraX Preview ──────────────────────────────────────
                    CameraPreview(
                        isTorchOn  = uiState.isTorchOn,
                        onQRDetected = { rawValue ->

                            // ── Debounce: ignore scans within 1.5 seconds ────
                            val now = System.currentTimeMillis()
                            if (now - lastScanTime < 1500L) return@CameraPreview
                            if (uiState.isProcessing)       return@CameraPreview
                            if (uiState.locationInput.isBlank()) {
                                uiState = uiState.copy(error = "Enter location before scanning")
                                return@CameraPreview
                            }

                            lastScanTime = now

                            // ── Parse QR payload ─────────────────────────────
                            val packageId = QRParser.extractPackageId(rawValue)
                            if (packageId == null) {
                                uiState = uiState.copy(error = "Invalid QR code — not a valid package")
                                return@CameraPreview
                            }

                            uiState = uiState.copy(isProcessing = true)
                            viewModel.submitScan(packageId, uiState.locationInput.trim())
                        }
                    )

                    // ── Scan frame overlay ───────────────────────────────────
                    ScanOverlay()

                    // ══════════════════════════════════════════════════════════
                    //  Top Controls — frosted glass circles
                    // ══════════════════════════════════════════════════════════
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Back button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlassBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        // Torch button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlassBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                uiState = uiState.copy(isTorchOn = !uiState.isTorchOn)
                            }) {
                                Icon(
                                    imageVector = if (uiState.isTorchOn)
                                        Icons.Default.FlashOff else Icons.Default.FlashOn,
                                    contentDescription = "Toggle torch",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // ══════════════════════════════════════════════════════════
                    //  Bottom Panel — Frosted Glass
                    // ══════════════════════════════════════════════════════════
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(GlassWhite)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Scanning indicator
                        if (uiState.isProcessing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Animated ping dot
                                val infiniteTransition = rememberInfiniteTransition(label = "scanPing")
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 0.8f, targetValue = 1.5f,
                                    animationSpec = infiniteRepeatable(
                                        tween(600), RepeatMode.Reverse
                                    ), label = "pingScale"
                                )
                                Box(
                                    modifier = Modifier
                                        .size((8 * scale).dp)
                                        .clip(CircleShape)
                                        .background(EmeraldActive)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "SCANNING...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = EmeraldActive
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        } else {
                            EditorialLabel(
                                text = "Position QR code within frame",
                                color = OnSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Location input
                        EditorialTextField(
                            value = uiState.locationInput,
                            onValueChange = {
                                if (it.length <= 300)
                                    uiState = uiState.copy(locationInput = it)
                            },
                            label = "Current Location",
                            placeholder = "e.g. Library Room 3B",
                            leadingIcon = Icons.Outlined.LocationOn,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions.Default
                        )
                        Spacer(Modifier.height(16.dp))

                        // Manual entry button
                        GradientButton(
                            text = "Capture Manual ID",
                            icon = Icons.Outlined.ArrowForward,
                            onClick = { /* TODO: Manual entry */ }
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "HOLD STEADY • AUTO-DETECT ENABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 9.sp
                            ),
                            color = OnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ── Permission denied — show rationale ───────────────────────────
            cameraPermission.status.shouldShowRationale -> {
                PermissionRationaleScreen(
                    onRequest = { cameraPermission.launchPermissionRequest() }
                )
            }

            // ── Permission permanently denied ─────────────────────────────────
            else -> {
                PermissionDeniedScreen(
                    onOpenSettings = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                )
            }
        }
    }
}

// ── CameraX Preview Composable ────────────────────────────────────────────────
@Composable
private fun CameraPreview(
    isTorchOn   : Boolean,
    onQRDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor       = remember { Executors.newSingleThreadExecutor() }
    var camera         by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(isTorchOn) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(imageProxy, onQRDetected)
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
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

// ── ML Kit image analysis ─────────────────────────────────────────────────────
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy  : ImageProxy,
    onQRDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image   = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                    ?.let { onQRDetected(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

// ── Scan Frame Overlay with Corner Indicators ─────────────────────────────────
@Composable
private fun ScanOverlay() {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(Color.Transparent)
        ) {
            val cornerColor = GradientStart
            val cornerSize  = 48.dp
            val strokeWidth = 4.dp

            // Top-left
            Box(Modifier.align(Alignment.TopStart)) {
                Divider(modifier = Modifier.width(cornerSize).padding(top = strokeWidth), color = cornerColor, thickness = strokeWidth)
                Divider(modifier = Modifier.height(cornerSize).padding(start = strokeWidth), color = cornerColor, thickness = strokeWidth)
            }
            // Top-right
            Box(Modifier.align(Alignment.TopEnd)) {
                Divider(modifier = Modifier.width(cornerSize).padding(top = strokeWidth), color = cornerColor, thickness = strokeWidth)
                Divider(modifier = Modifier.height(cornerSize).padding(end = strokeWidth).align(Alignment.TopEnd), color = cornerColor, thickness = strokeWidth)
            }
            // Bottom-left
            Box(Modifier.align(Alignment.BottomStart)) {
                Divider(modifier = Modifier.width(cornerSize).padding(bottom = strokeWidth).align(Alignment.BottomStart), color = cornerColor, thickness = strokeWidth)
                Divider(modifier = Modifier.height(cornerSize).padding(start = strokeWidth).align(Alignment.BottomStart), color = cornerColor, thickness = strokeWidth)
            }
            // Bottom-right
            Box(Modifier.align(Alignment.BottomEnd)) {
                Divider(modifier = Modifier.width(cornerSize).padding(bottom = strokeWidth).align(Alignment.BottomEnd), color = cornerColor, thickness = strokeWidth)
                Divider(modifier = Modifier.height(cornerSize).padding(end = strokeWidth).align(Alignment.BottomEnd), color = cornerColor, thickness = strokeWidth)
            }

            // Laser pulse animation
            val infiniteTransition = rememberInfiniteTransition(label = "laser")
            val laserOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(2000, easing = LinearEasing),
                    RepeatMode.Reverse
                ),
                label = "laserSweep"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = (260 * laserOffset).dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GradientStart.copy(alpha = 0.8f),
                                GradientEnd.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

// ── Permission rationale screen ───────────────────────────────────────────────
@Composable
private fun PermissionRationaleScreen(onRequest: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Camera Permission Required",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = OnSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "This app needs camera access to scan QR codes on packages.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = "Grant Permission",
            onClick = onRequest
        )
    }
}

// ── Permission permanently denied screen ──────────────────────────────────────
@Composable
private fun PermissionDeniedScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Camera Access Denied",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = OnSurface
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "Camera permission was permanently denied. Please enable it from Settings to use the scanner.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = OnSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(
            text = "Open Settings",
            onClick = onOpenSettings
        )
    }
}