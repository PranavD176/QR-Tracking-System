package com.ganesh.qrtracker.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VisibilityOff
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
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.*

// ── State holder — Member 2 will replace with ViewModel state ────────────────
data class RegisterUiState(
    val fullName            : String  = "",
    val email               : String  = "",
    val password            : String  = "",
    val confirmPassword     : String  = "",
    val isPasswordVisible   : Boolean = false,
    val isConfirmVisible    : Boolean = false,
    val isLoading           : Boolean = false,
    val acceptedTerms       : Boolean = true,
    val error               : String? = null,
    val showDiscardDialog   : Boolean = false
)

@Composable
fun RegisterScreen(navController: NavController) {

    var uiState       by remember { mutableStateOf(RegisterUiState()) }
    val focusManager  = LocalFocusManager.current
    val scrollState   = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Back press → show discard dialog ────────────────────────────────────
    BackHandler {
        uiState = uiState.copy(showDiscardDialog = true)
    }

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    // ── Discard dialog ───────────────────────────────────────────────────────
    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { uiState = uiState.copy(showDiscardDialog = false) },
            title   = { Text("Discard Registration?") },
            text    = { Text("All entered information will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    uiState = uiState.copy(showDiscardDialog = false)
                    navController.popBackStack()
                }) { Text("Discard", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = {
                    uiState = uiState.copy(showDiscardDialog = false)
                }) { Text("Keep Editing") }
            }
        )
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Surface
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // ══════════════════════════════════════════════════════════════════
            //  Large Editorial Gradient Header
            // ══════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(SignatureGradient),
                contentAlignment = Alignment.BottomStart
            ) {
                // Decorative blur elements
                Box(
                    modifier = Modifier
                        .size(256.dp)
                        .offset(x = 80.dp, y = (-48).dp)
                        .blur(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp)
                ) {
                    // "Start your journey" pill badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.QrCode2,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "START YOUR JOURNEY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Display heading
                    Text(
                        text = "Create\nAccount.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 48.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════════
            //  Content Canvas — overlapping form
            // ══════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(Surface)
                    .padding(horizontal = 32.dp, vertical = 36.dp)
            ) {
                // ── Full Name ────────────────────────────────────────────────
                EditorialTextField(
                    value = uiState.fullName,
                    onValueChange = { uiState = uiState.copy(fullName = it) },
                    label = "Full Name",
                    placeholder = "John Doe",
                    leadingIcon = Icons.Outlined.Person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                Spacer(Modifier.height(16.dp))

                // ── Email ────────────────────────────────────────────────────
                EditorialTextField(
                    value = uiState.email,
                    onValueChange = { uiState = uiState.copy(email = it.trim()) },
                    label = "Email Address",
                    placeholder = "john@example.com",
                    leadingIcon = Icons.Outlined.Mail,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                Spacer(Modifier.height(16.dp))

                // ── Password ─────────────────────────────────────────────────
                EditorialTextField(
                    value = uiState.password,
                    onValueChange = { uiState = uiState.copy(password = it) },
                    label = "Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                uiState = uiState.copy(
                                    isPasswordVisible = !uiState.isPasswordVisible
                                )
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = OutlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                Spacer(Modifier.height(16.dp))

                // ── Confirm Password ─────────────────────────────────────────
                EditorialTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { uiState = uiState.copy(confirmPassword = it) },
                    label = "Confirm Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Outlined.VerifiedUser,
                    visualTransformation = if (uiState.isConfirmVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                Spacer(Modifier.height(16.dp))

                // ── Terms checkbox ───────────────────────────────────────────
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (uiState.acceptedTerms) SurfaceContainerHigh
                                else SurfaceContainerHigh
                            )
                            .clickable {
                                uiState = uiState.copy(acceptedTerms = !uiState.acceptedTerms)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.acceptedTerms) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = CoralPrimaryFixed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "I agree to the ",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CoralPrimaryFixed,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                    Text(
                        text = " and ",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CoralPrimaryFixed,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
                Spacer(Modifier.height(24.dp))

                // ── Create Account button ────────────────────────────────────
                GradientButton(
                    text = "Create Account",
                    icon = Icons.Default.ArrowForward,
                    isLoading = uiState.isLoading,
                    onClick = {
                        when {
                            uiState.fullName.isBlank() ->
                                uiState = uiState.copy(error = "Full name cannot be empty")
                            uiState.fullName.trim().length < 2 ->
                                uiState = uiState.copy(error = "Name must be at least 2 characters")
                            uiState.email.isBlank() ->
                                uiState = uiState.copy(error = "Email cannot be empty")
                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(uiState.email).matches() ->
                                uiState = uiState.copy(error = "Enter a valid email address")
                            uiState.password.length < 8 ->
                                uiState = uiState.copy(error = "Password must be at least 8 characters")
                            !uiState.password.any { it.isUpperCase() } ->
                                uiState = uiState.copy(error = "Password must contain at least 1 uppercase letter")
                            !uiState.password.any { it.isDigit() } ->
                                uiState = uiState.copy(error = "Password must contain at least 1 number")
                            !uiState.password.any { !it.isLetterOrDigit() } ->
                                uiState = uiState.copy(error = "Password must contain at least 1 special character")
                            uiState.password != uiState.confirmPassword ->
                                uiState = uiState.copy(error = "Passwords do not match")
                            !uiState.acceptedTerms ->
                                uiState = uiState.copy(error = "Please accept the Terms of Service")
                            else -> {
                                // ── MOCK: remove when Member 2 adds ViewModel ────
                                // TODO: replace with viewModel.register(fullName, email, password)
                                navController.navigate(Routes.PACKAGE_LIST) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(24.dp))

                // ── Login link ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Log in",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CoralPrimaryFixed,
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}