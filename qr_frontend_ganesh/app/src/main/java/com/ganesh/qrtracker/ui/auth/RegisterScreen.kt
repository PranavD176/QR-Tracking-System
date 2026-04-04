package com.ganesh.qrtracker.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes

// ── State holder — Member 2 will replace with ViewModel state ────────────────
data class RegisterUiState(
    val fullName            : String  = "",
    val email               : String  = "",
    val password            : String  = "",
    val confirmPassword     : String  = "",
    val isPasswordVisible   : Boolean = false,
    val isConfirmVisible    : Boolean = false,
    val isLoading           : Boolean = false,
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
                }) { Text("Discard", color = MaterialTheme.colorScheme.error) }
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // ── Header ───────────────────────────────────────────────────────
            Text(
                text       = "Create Account",
                style      = MaterialTheme.typography.headlineLarge,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Fill in the details below to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            // ── Full Name ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = uiState.fullName,
                onValueChange = { uiState = uiState.copy(fullName = it) },
                label         = { Text("Full Name") },
                leadingIcon   = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine    = true,
                isError       = uiState.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            // ── Email ────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = uiState.email,
                onValueChange = { uiState = uiState.copy(email = it.trim()) },
                label         = { Text("Email") },
                leadingIcon   = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine    = true,
                isError       = uiState.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            // ── Password ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = uiState.password,
                onValueChange = { uiState = uiState.copy(password = it) },
                label         = { Text("Password") },
                leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon  = {
                    IconButton(onClick = {
                        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                    }) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.isPasswordVisible)
                                "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                isError       = uiState.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            // ── Confirm Password ─────────────────────────────────────────────
            OutlinedTextField(
                value         = uiState.confirmPassword,
                onValueChange = { uiState = uiState.copy(confirmPassword = it) },
                label         = { Text("Confirm Password") },
                leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon  = {
                    IconButton(onClick = {
                        uiState = uiState.copy(isConfirmVisible = !uiState.isConfirmVisible)
                    }) {
                        Icon(
                            imageVector = if (uiState.isConfirmVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.isConfirmVisible)
                                "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (uiState.isConfirmVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                isError       = uiState.error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(28.dp))

            // ── Register button ──────────────────────────────────────────────
            Button(
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
                        else -> {
                            // ── MOCK: remove when Member 2 adds ViewModel ────
                            // TODO: replace with viewModel.register(fullName, email, password)
                            navController.navigate(Routes.PACKAGE_LIST) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                },
                enabled  = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text  = "Create Account",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Navigate to Login ────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "Login",
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}