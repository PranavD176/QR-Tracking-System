package com.qrtracker.tracko.ui.alerts

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
//  App Alerts — System Notifications
//  Opened from the top bell icon (not bottom nav)
//  Backend-ready: replace mock data with ViewModel when API is available
// ══════════════════════════════════════════════════════════════════════════════

// ── Notification Category ────────────────────────────────────────────────────
enum class NotificationCategory { MAINTENANCE, SECURITY, POLICY, SYSTEM }

// ── Notification Data ────────────────────────────────────────────────────────
data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val category: NotificationCategory,
    val timeLabel: String,
    val isUnread: Boolean = false,
    val scheduledTime: String? = null
)

// ── Mock Data ────────────────────────────────────────────────────────────────
private val mockNotifications = listOf(
    AppNotification(
        "1", "System Maintenance",
        "Infrastructure maintenance scheduled for 02:00 AM. Cloud syncing may be temporarily delayed.",
        NotificationCategory.MAINTENANCE, "Scheduled", true, "Today, 02:00 AM"
    ),
    AppNotification(
        "2", "Security Update",
        "New validation protocol for high-value items is now mandatory. Biometric auth required for Tier-1 assets.",
        NotificationCategory.SECURITY, "2h ago"
    ),
    AppNotification(
        "3", "New Hub Policy",
        "Updated scanning procedures for international shipments. Ensure QR clarity before palletizing.",
        NotificationCategory.POLICY, "Yesterday"
    ),
)

@Composable
fun AppAlertsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf(mockNotifications) }

    Scaffold(
        containerColor = Surface,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassWhite)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Back button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLow)
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = OnSurface, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "App Notifications",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                brush = HorizontalBrandGradient
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mark all as read
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(SurfaceContainerLow)
                                .clickable {
                                    // TODO: Call ViewModel to mark all as read
                                    notifications = notifications.map { it.copy(isUnread = false) }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Mark all as read",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = CoralPrimary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                                .clickable { navController.navigate(Routes.USER_PROFILE) { launchSingleTop = true } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Hero Banner
            // ══════════════════════════════════════════════════════════════════
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CoralPrimary, PrimaryContainer)
                            )
                        )
                        .padding(28.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "SECURITY STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "High-Alert\nProtocols Active",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                lineHeight = 34.sp
                            ),
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        // Live monitoring pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                                label = "liveAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = alpha))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Live System Monitoring",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                    // Background icon
                    Icon(
                        Icons.Default.Shield,
                        null,
                        tint = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 16.dp)
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Section Label
            // ══════════════════════════════════════════════════════════════════
            item {
                Text(
                    "SYSTEM UPDATES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 8.dp)
                )
            }

            // ══════════════════════════════════════════════════════════════════
            //  Notification Cards
            // ══════════════════════════════════════════════════════════════════
            items(notifications) { notification ->
                NotificationCard(notification = notification)
            }

            // ══════════════════════════════════════════════════════════════════
            //  End of Notifications
            // ══════════════════════════════════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(48.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(CoralPrimary.copy(alpha = 0.3f), Color.Transparent)
                                ),
                                RoundedCornerShape(9999.dp)
                            )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "END OF NOTIFICATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp,
                            fontSize = 9.sp
                        ),
                        color = OnSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ── Notification Card ────────────────────────────────────────────────────────
@Composable
private fun NotificationCard(notification: AppNotification) {
    val categoryIcon: ImageVector = when (notification.category) {
        NotificationCategory.MAINTENANCE -> Icons.Outlined.Engineering
        NotificationCategory.SECURITY -> Icons.Outlined.Lock
        NotificationCategory.POLICY -> Icons.Outlined.Policy
        NotificationCategory.SYSTEM -> Icons.Outlined.Settings
    }

    val iconBg = if (notification.isUnread) {
        when (notification.category) {
            NotificationCategory.MAINTENANCE -> Color(0xFFFFF7ED) // orange-50
            else -> SurfaceContainerHighest
        }
    } else {
        SurfaceContainerHighest
    }

    val iconTint = if (notification.isUnread) {
        when (notification.category) {
            NotificationCategory.MAINTENANCE -> CoralPrimary
            else -> OnSurfaceVariant
        }
    } else {
        OnSurfaceVariant
    }

    val categoryLabel = when (notification.category) {
        NotificationCategory.MAINTENANCE -> "MAINTENANCE"
        NotificationCategory.SECURITY -> "SECURITY"
        NotificationCategory.POLICY -> "POLICY"
        NotificationCategory.SYSTEM -> "SYSTEM"
    }

    val catLabelBg = if (notification.isUnread) {
        CoralPrimaryFixed.copy(alpha = 0.15f)
    } else {
        SurfaceContainerHighest
    }

    val catLabelColor = if (notification.isUnread) CoralPrimary else OnSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (notification.isUnread) SurfaceContainerLowest
                    else SurfaceContainerLow
                )
                .padding(20.dp)
        ) {
            // Unread indicator bar
            if (notification.isUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-20).dp)
                        .width(3.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(CoralPrimary)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon, null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Category chip + time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(catLabelBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                categoryLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = catLabelColor
                            )
                        }
                        Text(
                            notification.timeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = OnSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // Title
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = OnSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    // Description
                    Text(
                        notification.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    // Scheduled time
                    if (notification.scheduledTime != null) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                notification.scheduledTime,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CoralPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

