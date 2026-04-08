package com.ganesh.qrtracker.ui.scan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.utils.QRParser
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
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
    val snackbarHostState = remember { SnackbarHostState() }

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

                            // ── MOCK: remove when Member 2 adds ViewModel ────
                            // TODO: replace with viewModel.scan(packageId, locationInput)
                            // Mock navigates to result screen with dummy data
                            navController.navigate(
                                Routes.scanResult(
                                    result      = "valid",
                                    packageDesc = "Mock Package",
                                    ownerName   = "Test User",
                                    alertSent   = false
                                )
                            )

                            uiState = uiState.copy(isProcessing = false)
                        }
                    )

                    // ── Scan frame overlay ───────────────────────────────────
                    ScanOverlay()

                    // ── Top bar ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            uiState = uiState.copy(isTorchOn = !uiState.isTorchOn)
                        }) {
                            Icon(
                                imageVector = if (uiState.isTorchOn)
                                    Icons.Default.FlashOff else Icons.Default.FlashOn,
                                contentDescription = "Toggle torch",
                                tint = Color.White
                            )
                        }
                    }

                    // ── Bottom panel — location input ────────────────────────
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text  = "Point camera at a QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value         = uiState.locationInput,
                            onValueChange = {
                                if (it.length <= 300)
                                    uiState = uiState.copy(locationInput = it)
                            },
                            label         = { Text("Location", color = Color.White) },
                            placeholder   = { Text("e.g. Library Room 3B", color = Color.Gray) },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedTextColor     = Color.White,
                                unfocusedTextColor   = Color.White,
                                focusedBorderColor   = Color.White,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                color    = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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
    val context        = LocalContext.current
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

// ── Scan frame overlay ────────────────────────────────────────────────────────
@Composable
private fun ScanOverlay() {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(Color.Transparent)
        ) {
            // Corner indicators
            val cornerColor = Color.White
            val cornerSize  = 40.dp
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
        }
    }
}

// ── Permission rationale screen ───────────────────────────────────────────────
@Composable
private fun PermissionRationaleScreen(onRequest: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Camera Permission Required",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "This app needs camera access to scan QR codes on packages.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text("Grant Permission") }
    }
}

// ── Permission permanently denied screen ──────────────────────────────────────
@Composable
private fun PermissionDeniedScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Camera Access Denied",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "Camera permission was permanently denied. Please enable it from Settings to use the scanner.",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenSettings) { Text("Open Settings") }
    }
}