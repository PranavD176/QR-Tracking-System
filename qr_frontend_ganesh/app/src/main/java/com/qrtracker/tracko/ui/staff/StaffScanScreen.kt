package com.qrtracker.tracko.ui.staff

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.scan.SharedCameraPreview
import com.qrtracker.tracko.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// ══════════════════════════════════════════════════════════════════════════════
//  Staff Scan — Full-Screen QR Scanner for Checkpoint Staff
//  Pixel-accurate to: stitch_qr_tracker_app_ui/staff_scan
//  Backend-ready: results route to StaffScanResultScreen
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StaffScanScreen(navController: NavController) {
    // Helper for testing different states based on ID input
    val resolveStatus = { id: String ->
        when (id) {
            "1" -> "misplaced"
            "2" -> "success"
            "3" -> "delivered"
            else -> "success"
        }
    }

    var manualParcelId by remember { mutableStateOf("") }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var hasProcessedScan by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Camera Preview (Full screen background) ──────────────────────────
        if (cameraPermission.status.isGranted) {
            SharedCameraPreview(
                onBarcodeDetected = { rawValue ->
                    if (!hasProcessedScan) {
                        hasProcessedScan = true
                        // TODO: Replace with actual API call to determine status
                        navController.navigate(
                            Routes.staffScanResult(
                                orderId = rawValue,
                                status = resolveStatus(rawValue),
                                currentCheckpoint = "Berlin-BER",
                                nextCheckpoint = "Munich-MUC"
                            )
                        ) { launchSingleTop = true }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder when camera not available
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Camera permission required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ── Scanner Frame Overlay ────────────────────────────────────────────
        ScannerFrameOverlay()

        // ── Top Navigation Bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // LIVE SCANNER pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "liveDot"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer.copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "LIVE SCANNER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }

            // Flashlight toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { /* TODO: Toggle flashlight */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlashlightOn, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // ── Feedback Text ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Align QR Code within the frame",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Scanning will happen automatically\nonce detected.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // ── Bottom Manual Input Panel ────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Manual Input",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnSurface
                )
                Icon(Icons.Default.Keyboard, null, tint = OutlineVariant, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(16.dp))

            BasicTextField(
                value = manualParcelId,
                onValueChange = { manualParcelId = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (manualParcelId.isNotBlank()) {
                        navController.navigate(
                            Routes.staffScanResult(
                                orderId = manualParcelId,
                                status = resolveStatus(manualParcelId),
                                currentCheckpoint = "Berlin-BER",
                                nextCheckpoint = "Munich-MUC"
                            )
                        ) { launchSingleTop = true }
                    }
                }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (manualParcelId.isEmpty()) {
                            Text(
                                "Enter Parcel ID manually",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OutlineVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(CoralPrimary, CoralSecondary)
                        )
                    )
                    .clickable {
                        if (manualParcelId.isNotBlank()) {
                            navController.navigate(
                                Routes.staffScanResult(
                                    orderId = manualParcelId,
                                    status = resolveStatus(manualParcelId),
                                    currentCheckpoint = "Berlin-BER",
                                    nextCheckpoint = "Munich-MUC"
                                )
                            ) { launchSingleTop = true }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Submit Parcel ID",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Scanner Frame Overlay ────────────────────────────────────────────────────
@Composable
private fun ScannerFrameOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scanner frame
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(3.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            // Corner accents - Top Left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(32.dp)
                    .offset(x = (-2).dp, y = (-2).dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(PrimaryContainer))
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(PrimaryContainer))
            }
            // Corner accents - Top Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .offset(x = 2.dp, y = (-2).dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.TopEnd).background(PrimaryContainer))
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).align(Alignment.TopEnd).background(PrimaryContainer))
            }
            // Corner accents - Bottom Left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(32.dp)
                    .offset(x = (-2).dp, y = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomStart).background(PrimaryContainer))
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).align(Alignment.BottomStart).background(PrimaryContainer))
            }
            // Corner accents - Bottom Right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .offset(x = 2.dp, y = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomEnd).background(PrimaryContainer))
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).align(Alignment.BottomEnd).background(PrimaryContainer))
            }

            // Scanning line animation
            val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 272f,
                animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
                label = "scanLineY"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = yOffset.dp)
                    .padding(horizontal = 4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GradientStart, Color.Transparent)
                        )
                    )
            )
        }
    }
}
