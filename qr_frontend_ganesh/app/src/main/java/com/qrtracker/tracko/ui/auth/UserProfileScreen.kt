package com.qrtracker.tracko.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Logout
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
import com.qrtracker.tracko.ui.theme.SurfaceContainerLowest
import com.qrtracker.tracko.utils.TokenManager

@Composable
fun UserProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Read stored profile from TokenManager
    val fullName = tokenManager.getFullName() ?: "User"
    val email = tokenManager.getEmail() ?: "—"
    val userId = tokenManager.getUserId() ?: "—"
    val role = tokenManager.getRole() ?: "user"
    val contactNo = tokenManager.getContactNo() ?: "—"
    val initial = fullName.firstOrNull()?.toString() ?: "U"

    val userDetails = linkedMapOf(
        "Name" to fullName,
        "User ID" to userId,
        "Email" to email,
        "Contact No" to contactNo,
        "Role" to role.replaceFirstChar { it.uppercase() },
        "Account Status" to "Active"
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
                        "User Details",
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
                        .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(SurfaceContainerHigh, CircleShape)
                                .border(1.dp, SurfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initial, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                fullName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                "${role.replaceFirstChar { it.uppercase() }} User",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(ReceivedGreenBg)
                                    .border(1.dp, ReceivedGreenBorder, RoundedCornerShape(9999.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Active User",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ReceivedGreen
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    userDetails.forEach { (key, value) ->
                        DetailLine(label = key, value = value)
                    }

                    Spacer(Modifier.height(20.dp))

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
                        "All sessions will be cleared upon signing out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
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

