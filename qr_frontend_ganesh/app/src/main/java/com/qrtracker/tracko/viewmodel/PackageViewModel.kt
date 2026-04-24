package com.qrtracker.tracko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.CreatePackageRequest
import com.qrtracker.tracko.network.models.PackageResponse
import com.qrtracker.tracko.network.models.ScanHistoryResponse
import com.qrtracker.tracko.network.models.UpdateCheckpointsRequest
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

// Represents state for fetching users (any logged-in user can call)
sealed class UsersState {
    object Idle : UsersState()
    object Loading : UsersState()
    data class Success(val users: List<UserResponse>) : UsersState()
    data class Error(val message: String) : UsersState()
}

// Represents every possible state for creating a package
sealed class CreatePackageState {
    object Idle : CreatePackageState()
    object Loading : CreatePackageState()
    data class Success(val packageId: String, val qrPayload: String) : CreatePackageState()
    data class Error(val message: String) : CreatePackageState()
}

sealed class UpdateCheckpointsState {
    object Idle : UpdateCheckpointsState()
    object Loading : UpdateCheckpointsState()
    object Success : UpdateCheckpointsState()
    data class Error(val message: String) : UpdateCheckpointsState()
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

    // ─── USERS STATE ─────────────────────────────────────────────────

    private val _usersState = MutableStateFlow<UsersState>(UsersState.Idle)
    val usersState: StateFlow<UsersState> = _usersState

    private val _updateCheckpointsState = MutableStateFlow<UpdateCheckpointsState>(UpdateCheckpointsState.Idle)
    val updateCheckpointsState: StateFlow<UpdateCheckpointsState> = _updateCheckpointsState
    private var packageCacheAtMs: Long = 0L
    private var usersCacheAtMs: Long = 0L

    companion object {
        private const val CACHE_TTL_MS = 30_000L
    }

    // ─── FETCH PACKAGES ──────────────────────────────────────────────

    fun fetchPackages(status: String? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (!forceRefresh &&
                status == null &&
                _packageListState.value is PackageListState.Success &&
                now - packageCacheAtMs < CACHE_TTL_MS
            ) {
                return@launch
            }

            _packageListState.value = PackageListState.Loading

            try {
                val response = apiService.getPackages(status = status)

                if (response.isSuccessful && response.body()?.success == true) {
                    val packages = response.body()!!.data ?: emptyList()
                    _packageListState.value = PackageListState.Success(packages)
                    if (status == null) {
                        packageCacheAtMs = System.currentTimeMillis()
                    }

                } else {
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

    // ─── FETCH USERS ─────────────────────────────────────────────────

    fun fetchUsers(search: String? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (!forceRefresh &&
                search.isNullOrBlank() &&
                _usersState.value is UsersState.Success &&
                now - usersCacheAtMs < CACHE_TTL_MS
            ) {
                return@launch
            }

            _usersState.value = UsersState.Loading

            try {
                val response = apiService.getUsers(search = search)

                if (response.isSuccessful && response.body()?.success == true) {
                    val users = response.body()!!.data ?: emptyList()
                    _usersState.value = UsersState.Success(users)
                    if (search.isNullOrBlank()) {
                        usersCacheAtMs = System.currentTimeMillis()
                    }
                } else {
                    if (response.code() == 401) {
                        _usersState.value = UsersState.Error("Session expired.")
                    } else {
                        val errorMsg = response.body()?.error ?: "Failed to fetch users"
                        _usersState.value = UsersState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                _usersState.value = UsersState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── CREATE PACKAGE ──────────────────────────────────────────────

    fun createPackage(
        description: String,
        receiverId: String,
        routeCheckpoints: List<String>? = null
    ) {
        viewModelScope.launch {
            _createPackageState.value = CreatePackageState.Loading

            try {
                val response = apiService.createPackage(
                    CreatePackageRequest(description, receiverId, routeCheckpoints)
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

    // ─── ACCEPT/REJECT PACKAGE ───────────────────────────────────────

    fun acceptPackage(packageId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.acceptPackage(packageId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchPackages(forceRefresh = true) // Refresh list
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun rejectPackage(packageId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.rejectPackage(packageId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchPackages(forceRefresh = true) // Refresh list
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun updateCheckpoints(packageId: String, checkpoints: List<String>) {
        viewModelScope.launch {
            _updateCheckpointsState.value = UpdateCheckpointsState.Loading
            try {
                val cleanCheckpoints = checkpoints.map { it.trim() }.filter { it.isNotBlank() }
                val response = apiService.updateCheckpoints(
                    packageId = packageId,
                    request = UpdateCheckpointsRequest(route_checkpoints = cleanCheckpoints)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _updateCheckpointsState.value = UpdateCheckpointsState.Success
                    fetchPackages(forceRefresh = true)
                    fetchScanHistory(packageId)
                } else {
                    val errorMsg = response.body()?.error ?: "Failed to update route checkpoints"
                    _updateCheckpointsState.value = UpdateCheckpointsState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _updateCheckpointsState.value = UpdateCheckpointsState.Error("Network error: ${e.message}")
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

    fun resetUpdateCheckpointsState() {
        _updateCheckpointsState.value = UpdateCheckpointsState.Idle
    }
}
