package com.qrtracker.tracko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.CreatePackageRequest
import com.qrtracker.tracko.network.models.PackageResponse
import com.qrtracker.tracko.network.models.ScanHistoryResponse
import com.qrtracker.tracko.network.models.UserResponse
import com.qrtracker.tracko.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents every possible state for the package list screen
sealed class PackageListState {
    object Idle : PackageListState()
    object Loading : PackageListState()
    data class Success(val packages: List<PackageResponse>) : PackageListState()
    data class Error(val message: String) : PackageListState()
}

// Represents every possible state for the scan history screen
sealed class ScanHistoryState {
    object Idle : ScanHistoryState()
    object Loading : ScanHistoryState()
    data class Success(val scans: List<ScanHistoryResponse>) : ScanHistoryState()
    data class Error(val message: String) : ScanHistoryState()
}

// Represents state for admin fetching users
sealed class AdminUsersState {
    object Idle : AdminUsersState()
    object Loading : AdminUsersState()
    data class Success(val users: List<UserResponse>) : AdminUsersState()
    data class Error(val message: String) : AdminUsersState()
}

// Represents every possible state for creating a package
sealed class CreatePackageState {
    object Idle : CreatePackageState()
    object Loading : CreatePackageState()
    data class Success(val packageId: String, val qrPayload: String) : CreatePackageState()
    data class Error(val message: String) : CreatePackageState()
}

class PackageViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // ─── PACKAGE LIST STATE ──────────────────────────────────────────

    private val _packageListState = MutableStateFlow<PackageListState>(PackageListState.Idle)
    val packageListState: StateFlow<PackageListState> = _packageListState

    // ─── SCAN HISTORY STATE ──────────────────────────────────────────

    private val _scanHistoryState = MutableStateFlow<ScanHistoryState>(ScanHistoryState.Idle)
    val scanHistoryState: StateFlow<ScanHistoryState> = _scanHistoryState

    // ─── CREATE PACKAGE STATE ────────────────────────────────────────

    private val _createPackageState = MutableStateFlow<CreatePackageState>(CreatePackageState.Idle)
    val createPackageState: StateFlow<CreatePackageState> = _createPackageState

    // ─── ADMIN USERS STATE ───────────────────────────────────────────

    private val _adminUsersState = MutableStateFlow<AdminUsersState>(AdminUsersState.Idle)
    val adminUsersState: StateFlow<AdminUsersState> = _adminUsersState

    // ─── FETCH PACKAGES ──────────────────────────────────────────────

    // Called when Home Dashboard screen loads
    // Fetches all packages owned by the logged-in user
    fun fetchPackages(status: String? = null) {
        viewModelScope.launch {
            _packageListState.value = PackageListState.Loading

            try {
                val response = apiService.getPackages(status = status)

                if (response.isSuccessful && response.body()?.success == true) {
                    val packages = response.body()!!.data ?: emptyList()
                    _packageListState.value = PackageListState.Success(packages)

                } else {
                    // Check if token expired — 401 means re-login needed
                    if (response.code() == 401) {
                        _packageListState.value = PackageListState.Error("Session expired. Please login again.")
                    } else {
                        val errorMsg = response.body()?.error ?: "Failed to fetch packages"
                        _packageListState.value = PackageListState.Error(errorMsg)
                    }
                }

            } catch (e: Exception) {
                _packageListState.value = PackageListState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── FETCH SCAN HISTORY ──────────────────────────────────────────

    // Called when user taps on a specific package to see its scan history
    fun fetchScanHistory(packageId: String) {
        viewModelScope.launch {
            _scanHistoryState.value = ScanHistoryState.Loading

            try {
                val response = apiService.getPackageScans(packageId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val scans = response.body()!!.data ?: emptyList()
                    _scanHistoryState.value = ScanHistoryState.Success(scans)

                } else {
                    if (response.code() == 401) {
                        _scanHistoryState.value = ScanHistoryState.Error("Session expired. Please login again.")
                    } else {
                        val errorMsg = response.body()?.error ?: "Failed to fetch scan history"
                        _scanHistoryState.value = ScanHistoryState.Error(errorMsg)
                    }
                }

            } catch (e: Exception) {
                _scanHistoryState.value = ScanHistoryState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── FETCH ADMIN USERS ───────────────────────────────────────────

    fun fetchAdminUsers() {
        viewModelScope.launch {
            _adminUsersState.value = AdminUsersState.Loading

            try {
                val response = apiService.getAdminUsers()

                if (response.isSuccessful && response.body()?.success == true) {
                    val users = response.body()!!.data ?: emptyList()
                    _adminUsersState.value = AdminUsersState.Success(users)
                } else {
                    if (response.code() == 401) {
                        _adminUsersState.value = AdminUsersState.Error("Session expired.")
                    } else {
                        val errorMsg = response.body()?.error ?: "Failed to fetch users"
                        _adminUsersState.value = AdminUsersState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                _adminUsersState.value = AdminUsersState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── CREATE PACKAGE ──────────────────────────────────────────────

    // Called when user submits the Create Package form
    // Returns qr_payload which Member 1 uses to generate the QR image
    fun createPackage(
        description: String,
        destinationUserId: String? = null,
        destinationAddress: String? = null,
        routeCheckpoints: List<com.qrtracker.tracko.ui.packages.RouteCheckpoint>? = null
    ) {
        viewModelScope.launch {
            _createPackageState.value = CreatePackageState.Loading

            try {
                val response = apiService.createPackage(
                    CreatePackageRequest(description, destinationUserId, destinationAddress, routeCheckpoints)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val packageId = response.body()!!.data?.package_id ?: ""
                    val qrPayload = response.body()!!.data?.qr_payload ?: ""
                    _createPackageState.value = CreatePackageState.Success(packageId, qrPayload)

                } else {
                    if (response.code() == 401) {
                        _createPackageState.value = CreatePackageState.Error("Session expired. Please login again.")
                    } else {
                        val errorMsg = response.body()?.error ?: "Failed to create package"
                        _createPackageState.value = CreatePackageState.Error(errorMsg)
                    }
                }

            } catch (e: Exception) {
                _createPackageState.value = CreatePackageState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── RESET STATES ────────────────────────────────────────────────

    fun resetCreatePackageState() {
        _createPackageState.value = CreatePackageState.Idle
    }

    fun resetScanHistoryState() {
        _scanHistoryState.value = ScanHistoryState.Idle
    }
}
