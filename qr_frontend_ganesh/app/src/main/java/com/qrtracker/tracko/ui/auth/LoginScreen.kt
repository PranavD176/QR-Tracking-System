package com.qrtracker.tracko.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.qrtracker.tracko.ui.navigation.Routes
import com.qrtracker.tracko.ui.theme.*

// ── State holder — Member 2 will replace with ViewModel state ────────────────
data class LoginUiState(
    val email         : String  = "",
    val password      : String  = "",
    val isLoading     : Boolean = false,
    val isPasswordVisible: Boolean = false,
    val error         : String? = null
)

@Composable
fun LoginScreen(navController: NavController) {

    // ── Local state (temporary until Member 2 adds ViewModel) ────────────────
    var uiState by remember { mutableStateOf(LoginUiState()) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // ══════════════════════════════════════════════════════════════════
            //  Hero Header — Signature Gradient with Kinetic Pulse elements
            // ══════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.38f)
                    .defaultMinSize(minHeight = 280.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(SignatureGradient),
                contentAlignment = Alignment.BottomStart
            ) {
                // Decorative blur circles
                Box(
                    modifier = Modifier
                        .size(256.dp)
                        .offset(x = (-40).dp, y = (-80).dp)
                        .blur(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(x = (-20).dp, y = 40.dp)
                        .blur(48.dp)
                        .clip(CircleShape)
                        .background(GradientEnd.copy(alpha = 0.2f))
                        .align(Alignment.BottomStart)
                )

                // Content
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 40.dp)
                ) {
                    // App icon + name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "QR Tracker",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Logistics redefined\nthrough the Kinetic Pulse.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Content Canvas — overlaps hero
            // ══════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Surface)
                    .padding(horizontal = 32.dp, vertical = 36.dp)
            ) {
                // Welcome text
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign in to track your movement",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))

                // ── Email field ──────────────────────────────────────────────
                EditorialTextField(
                    value = uiState.email,
                    onValueChange = { uiState = uiState.copy(email = it.trim()) },
                    label = "Email Address",
                    placeholder = "alex@example.com",
                    trailingIcon = {
                        Icon(
                            Icons.Outlined.AlternateEmail,
                            contentDescription = null,
                            tint = OutlineVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.error != null
                )
                Spacer(Modifier.height(20.dp))

                // ── Password field ───────────────────────────────────────────
                // Label row with "Forgot?" link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PASSWORD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "FORGOT?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = CoralPrimaryFixed,
                        modifier = Modifier.clickable { /* TODO: Forgot password */ }
                    )
                }
                // Password input (without label since we render it above)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .border(
                            1.dp,
                            OutlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = uiState.password,
                            onValueChange = { uiState = uiState.copy(password = it) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = OnSurface
                            ),
                            singleLine = true,
                            visualTransformation = if (uiState.isPasswordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (uiState.password.isEmpty()) {
                                        Text(
                                            "••••••••",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                uiState = uiState.copy(
                                    isPasswordVisible = !uiState.isPasswordVisible
                                )
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LockOpen,
                                contentDescription = if (uiState.isPasswordVisible)
                                    "Hide password" else "Show password",
                                tint = OutlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Sign In button ───────────────────────────────────────────
                GradientButton(
                    text = "Sign In",
                    isLoading = uiState.isLoading,
                    onClick = {
                        when {
                            uiState.email.isBlank() ->
                                uiState = uiState.copy(error = "Email cannot be empty")
                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(uiState.email).matches() ->
                                uiState = uiState.copy(error = "Enter a valid email address")
                            uiState.password.isBlank() ->
                                uiState = uiState.copy(error = "Password cannot be empty")
                            else -> {
                                // ── Hardcoded Credentials ────
                                val enteredEmail = uiState.email.trim()
                                val enteredPass = uiState.password
                                
                                if (enteredEmail == "u@g.c" && enteredPass == "1") {
                                    // User login -> Home / Package List
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                } else if (enteredEmail == "a@g.c" && enteredPass == "1") {
                                    // Admin login -> Admin Checkpoint
                                    navController.navigate(Routes.ADMIN_CHECKPOINT) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                } else if (enteredEmail == "c@g.c" && enteredPass == "1") {
                                    // Staff login -> Staff Home
                                    navController.navigate(Routes.STAFF_HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                } else {
                                    uiState = uiState.copy(error = "Invalid credentials. Try testing accounts.")
                                }
                            }
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // ── Register link ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CoralPrimaryFixed,
                        modifier = Modifier.clickable {
                            navController.navigate(Routes.REGISTER)
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Spacer(Modifier.height(32.dp))

                // ── Version footer ───────────────────────────────────────────
                Text(
                    text = "VERSION 2.4.0 • KINETIC ENGINE V3",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 9.sp
                    ),
                    color = OutlineVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
