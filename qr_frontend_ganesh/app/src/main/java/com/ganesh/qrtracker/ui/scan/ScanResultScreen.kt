package com.ganesh.qrtracker.ui.scan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.ValidGreen
import com.ganesh.qrtracker.ui.theme.ValidGreenBg
import com.ganesh.qrtracker.ui.theme.MisplacedRed
import com.ganesh.qrtracker.ui.theme.MisplacedRedBg

@Composable
fun ScanResultScreen(
    navController : NavController,
    result        : String,
    packageDesc   : String,
    ownerName     : String,
    alertSent     : Boolean
) {
    // ── Block back navigation — result screen is not re-entrant ─────────────
    BackHandler { /* consumed — user must use buttons below */ }

    val isValid      = result == "valid"
    val bgColor      = if (isValid) ValidGreenBg  else MisplacedRedBg
    val accentColor  = if (isValid) ValidGreen    else MisplacedRed
    val icon         = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning
    val headline     = if (isValid) "Package Verified" else "Misplaced Package"
    val subtext      = if (isValid)
        "This package belongs to you."
    else
        "Alert sent to $ownerName"

    // ── Icon pulse animation ─────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.08f,
        animationSpec  = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    // ── Entry animation ──────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec  = tween(400)
        )
    ) {
        Box(
            modifier          = Modifier
                .fillMaxSize()
                .background(bgColor),
            contentAlignment  = Alignment.Center
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Status icon ──────────────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = headline,
                        tint               = accentColor,
                        modifier           = Modifier.size(60.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Headline ─────────────────────────────────────────────────
                Text(
                    text       = headline,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // ── Subtext ──────────────────────────────────────────────────
                Text(
                    text      = subtext,
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = accentColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                // ── Package info card ────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultRow(label = "Package",  value = packageDesc)
                        ResultRow(label = "Owner",    value = ownerName)
                        ResultRow(label = "Status",   value = if (isValid) "✓ Valid" else "⚠ Misplaced")
                        if (!isValid && alertSent) {
                            ResultRow(label = "Alert", value = "Sent to owner")
                        }
                    }
                }

                Spacer(Modifier.height(36.dp))

                // ── Scan Another button ──────────────────────────────────────
                Button(
                    onClick = {
                        navController.navigate(Routes.SCANNER) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    )
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Scan Another",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Go Home button ───────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        navController.navigate(Routes.PACKAGE_LIST) {
                            popUpTo(Routes.PACKAGE_LIST) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Go Home",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ── Reusable row inside result card ───────────────────────────────────────────
@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.Gray,
            modifier  = Modifier.weight(0.35f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = Color.Black,
            modifier   = Modifier.weight(0.65f),
            textAlign  = TextAlign.End
        )
    }
}