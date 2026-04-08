package com.ganesh.qrtracker.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ganesh.qrtracker.ui.navigation.Routes
import com.ganesh.qrtracker.ui.theme.Navy
import com.ganesh.qrtracker.ui.theme.ValidGreen
import com.ganesh.qrtracker.ui.theme.ValidGreenBg

// ── State holder ─────────────────────────────────────────────────────────────
data class CreatePackageUiState(
    val description     : String  = "",
    val isLoading       : Boolean = false,
    val error           : String? = null,
    val createdPackageId: String? = null,   // non-null = success
    val createdQrPayload: String? = null    // QR string to display after creation
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePackageScreen(navController: NavController) {

    var uiState           by remember { mutableStateOf(CreatePackageUiState()) }
    val focusManager      = LocalFocusManager.current
    val scrollState       = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val charLimit         = 200

    // ── Show error in snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            uiState = uiState.copy(error = null)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Create Package",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Icon ─────────────────────────────────────────────────────────
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint     = Navy,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text       = "New Package",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text      = "Describe the package so you can identify it later",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            // ── Show success state after creation ────────────────────────────
            if (uiState.createdPackageId != null) {
                CreationSuccessCard(
                    packageId  = uiState.createdPackageId!!,
                    qrPayload  = uiState.createdQrPayload ?: "",
                    onGoHome   = {
                        navController.navigate(Routes.PACKAGE_LIST) {
                            popUpTo(Routes.PACKAGE_LIST) { inclusive = true }
                        }
                    },
                    onCreateAnother = {
                        uiState = CreatePackageUiState()
                    }
                )
            } else {

                // ── Description field ────────────────────────────────────────
                OutlinedTextField(
                    value         = uiState.description,
                    onValueChange = {
                        if (it.length <= charLimit)
                            uiState = uiState.copy(description = it)
                    },
                    label         = { Text("Package Description") },
                    placeholder   = { Text("e.g. Physics textbook — blue cover") },
                    leadingIcon   = {
                        Icon(Icons.Default.Inventory2, contentDescription = null)
                    },
                    supportingText = {
                        Text(
                            text  = "${uiState.description.length} / $charLimit",
                            color = if (uiState.description.length >= charLimit)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError       = uiState.error != null,
                    singleLine    = false,
                    maxLines      = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction      = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(28.dp))

                // ── Create button ────────────────────────────────────────────
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when {
                            uiState.description.isBlank() ->
                                uiState = uiState.copy(
                                    error = "Package description cannot be empty"
                                )
                            uiState.description.trim().length < 3 ->
                                uiState = uiState.copy(
                                    error = "Description must be at least 3 characters"
                                )
                            else -> {
                                // ── MOCK: remove when Member 2 adds ViewModel
                                // TODO: replace with viewModel.createPackage(description)
                                val mockId = "uuid-${System.currentTimeMillis()}"
                                uiState = uiState.copy(
                                    createdPackageId = mockId,
                                    createdQrPayload = "QR_TRACKING:$mockId"
                                )
                            }
                        }
                    },
                    enabled  = uiState.isLoading.not(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text  = "Create Package",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Success card shown after package creation ─────────────────────────────────
@Composable
private fun CreationSuccessCard(
    packageId      : String,
    qrPayload      : String,
    onGoHome       : () -> Unit,
    onCreateAnother: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = ValidGreenBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint     = ValidGreen,
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Package Created!",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = ValidGreen
            )
            Spacer(Modifier.height(16.dp))

            // ── QR payload display ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text  = "QR Payload",
                        style = MaterialTheme.typography.labelSmall,
                        color = ValidGreen.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = qrPayload,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = ValidGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── QR image placeholder ─────────────────────────────────────────
            // TODO: replace with ZXing QR image generated from qrPayload
            // when Member 2 provides real payload from POST /packages response
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Color.White.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "QR Code Image\n(Member 2 plugs in ZXing here)",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = ValidGreen.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Actions ──────────────────────────────────────────────────────
            Button(
                onClick  = onGoHome,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ValidGreen)
            ) {
                Text("Go to My Packages", color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick  = onCreateAnother,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = ValidGreen)
            ) {
                Text("Create Another")
            }
        }
    }
}