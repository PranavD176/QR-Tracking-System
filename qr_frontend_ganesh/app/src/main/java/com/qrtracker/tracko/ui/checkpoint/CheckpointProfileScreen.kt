package com.qrtracker.tracko.ui.checkpoint

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.ErrorRed
import com.qrtracker.tracko.ui.theme.GlassWhite
import com.qrtracker.tracko.ui.theme.GradientButton
import com.qrtracker.tracko.ui.theme.OnSurface
import com.qrtracker.tracko.ui.theme.OnSurfaceVariant
import com.qrtracker.tracko.ui.theme.ReceivedGreen
import com.qrtracker.tracko.ui.theme.ReceivedGreenBg
import com.qrtracker.tracko.ui.theme.ReceivedGreenBorder
import com.qrtracker.tracko.ui.theme.Surface
import com.qrtracker.tracko.ui.theme.SurfaceContainerHigh
import com.qrtracker.tracko.ui.theme.SurfaceContainerHighest
import com.qrtracker.tracko.ui.theme.SurfaceContainerLowest
import com.qrtracker.tracko.utils.TokenManager

@Composable
fun CheckpointProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    val checkpointDetails = linkedMapOf(
        "Checkpoint Name" to "Bhiwandi",
        "Location" to "Bhiwandi, Maharashtra",
        "Checkpoint ID" to "CHK-BHI-004",
        "Zone Code" to "MH-WEST-02",
        "Manager" to "Amit Sharma",
        "Contact" to "+91 98765 12045",
        "Email" to "bhiwandi.checkpoint@tracko.com",
        "Operating Hours" to "06:00 - 23:00",
        "Active Lanes" to "08 / 10",
        "Daily Throughput" to "1,240 parcels",
        "Shift Status" to "Live",
        "Sync Status" to "Connected"
    )

    Scaffold(containerColor = Surface) { padding ->
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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                    Text(
                        "Checkpoint Profile",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(22.dp))
                        .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "B",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Bhiwandi",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                "Bhiwandi, Maharashtra",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(ReceivedGreenBg)
                                        .border(1.dp, ReceivedGreenBorder, RoundedCornerShape(9999.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Operational",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ReceivedGreen
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(SurfaceContainerHighest)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "High Throughput",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStat(
                            label = "Checkpoint ID",
                            value = "CHK-BHI-004",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStat(
                            label = "Active Lanes",
                            value = "08/10",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        "Checkpoint Overview",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Primary sorting and verification hub for the western Maharashtra route network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    checkpointDetails.forEach { (label, value) ->
                        DetailLine(label = label, value = value)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        "Key Contacts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(14.dp))
                    ContactRow(
                        icon = Icons.Default.Person,
                        title = "Checkpoint Manager",
                        value = "Amit Sharma"
                    )
                    Spacer(Modifier.height(10.dp))
                    ContactRow(
                        icon = Icons.Default.Phone,
                        title = "Direct Line",
                        value = "+91 98765 12045"
                    )
                    Spacer(Modifier.height(10.dp))
                    ContactRow(
                        icon = Icons.Default.MailOutline,
                        title = "Operations Email",
                        value = "bhiwandi.checkpoint@tracko.com"
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        "Operating Notes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(14.dp))
                    NoteRow("24/7 security coverage and lane monitoring")
                    Spacer(Modifier.height(8.dp))
                    NoteRow("Priority route handling for express and overnight parcels")
                    Spacer(Modifier.height(8.dp))
                    NoteRow("Live sync with admin checkpoint dashboard")
                    Spacer(Modifier.height(8.dp))
                    NoteRow("Manual escalation available for misplaced or duplicate scans")
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(GlassWhite, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Checkpoint Status",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Bhiwandi is currently live and accepting parcel scans across all active lanes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    GradientButton(
                        text = "Back to Checkpoint",
                        icon = Icons.Default.Settings,
                        onClick = {
                            navController.navigate(Routes.ADMIN_CHECKPOINT) {
                                popUpTo(Routes.ADMIN_CHECKPOINT) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    GradientButton(
                        text = "Sign Out",
                        icon = Icons.Default.Logout,
                        onClick = {
                            tokenManager.clearAll()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Checkpoint access will end when signing out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = OnSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurface
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurface
        )
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(ReceivedGreen)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}
