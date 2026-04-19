package com.qrtracker.tracko.ui.admin

import androidx.compose.foundation.background
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
import com.qrtracker.tracko.ui.theme.Surface
import com.qrtracker.tracko.ui.theme.SurfaceContainerHigh
import com.qrtracker.tracko.ui.theme.SurfaceContainerLowest
import com.qrtracker.tracko.utils.TokenManager

@Composable
fun AdminProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Mock profile now; keep shape ready for backend response binding.
    val adminDetails = linkedMapOf(
        "Name" to "Alex Chen",
        "Age" to "34",
        "Admin ID" to "ADM-BER-0042",
        "Phone" to "+49 151 2244 8811",
        "Email" to "alex.chen@warehousehub.com",
        "Hub" to "Berlin-BER Hub",
        "Station" to "SCAN-04-B",
        "Role" to "Supervisor"
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
                        "Admin Details",
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
                                .background(SurfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OnSurfaceVariant)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Alex Chen",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                "Supervisor • Berlin-BER Hub",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    adminDetails.forEach { (key, value) ->
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
                        "Sign out is placed at the end as requested.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
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

