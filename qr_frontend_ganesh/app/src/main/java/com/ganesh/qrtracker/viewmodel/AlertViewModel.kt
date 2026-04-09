package com.ganesh.qrtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.qrtracker.network.RetrofitClient
import com.ganesh.qrtracker.network.models.AdminAlertResponse
import com.ganesh.qrtracker.network.models.AlertResponse
import com.ganesh.qrtracker.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AlertListState {
    object Idle : AlertListState()
    object Loading : AlertListState()
    data class Success(val alerts: List<AlertResponse>) : AlertListState()
    data class Error(val message: String) : AlertListState()
}

sealed class AdminAlertState {
    object Idle : AdminAlertState()
    object Loading : AdminAlertState()
    data class Success(val alerts: List<AdminAlertResponse>) : AdminAlertState()
    data class Error(val message: String) : AdminAlertState()
}

sealed class AcknowledgeState {
    object Idle : AcknowledgeState()
    object Loading : AcknowledgeState()
    data class Success(val alertId: String) : AcknowledgeState()
    data class Error(val message: String) : AcknowledgeState()
}

class AlertViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // ─── ALERT LIST STATE ────────────────────────────────────────────

    private val _alertListState = MutableStateFlow<AlertListState>(AlertListState.Idle)
    val alertListState: StateFlow<AlertListState> = _alertListState

    // ─── ADMIN ALERT STATE ───────────────────────────────────────────

    private val _adminAlertState = MutableStateFlow<AdminAlertState>(AdminAlertState.Idle)
    val adminAlertState: StateFlow<AdminAlertState> = _adminAlertState

    // ─── ACKNOWLEDGE STATE ───────────────────────────────────────────

    private val _acknowledgeState = MutableStateFlow<AcknowledgeState>(AcknowledgeState.Idle)
    val acknowledgeState: StateFlow<AcknowledgeState> = _acknowledgeState

    // ─── FETCH MY ALERTS ─────────────────────────────────────────────

    fun fetchAlerts(status: String? = "sent") {
        viewModelScope.launch {
            _alertListState.value = AlertListState.Loading

            try {
                val response = apiService.getAlerts(status = status)

                if (response.isSuccessful && response.body()?.success == true) {
                    val alerts = response.body()!!.data ?: emptyList()
                    _alertListState.value = AlertListState.Success(alerts)

                } else {
                    val errorMsg = response.body()?.error ?: "Failed to fetch alerts"
                    _alertListState.value = AlertListState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _alertListState.value = AlertListState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── ACKNOWLEDGE ALERT ───────────────────────────────────────────

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            _acknowledgeState.value = AcknowledgeState.Loading

            try {
                val response = apiService.acknowledgeAlert(alertId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _acknowledgeState.value = AcknowledgeState.Success(alertId)
                    fetchAlerts()

                } else {
                    val errorMsg = response.body()?.error ?: "Failed to acknowledge alert"
                    _acknowledgeState.value = AcknowledgeState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _acknowledgeState.value = AcknowledgeState.Error("Network error: ${e.message}")
            }
        }
    }

    // ─── FETCH ADMIN ALERTS ──────────────────────────────────────────

    fun fetchAdminAlerts(status: String? = "sent") {
        viewModelScope.launch {
            _adminAlertState.value = AdminAlertState.Loading

            try {
                val response = apiService.getAdminAlerts(status = status)

                if (response.isSuccessful && response.body()?.success == true) {
                    val alerts = response.body()!!.data ?: emptyList()
                    _adminAlertState.value = AdminAlertState.Success(alerts)

                } else {
                    val errorMsg = response.body()?.error ?: "Failed to fetch admin alerts"
                    _adminAlertState.value = AdminAlertState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _adminAlertState.value = AdminAlertState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetAcknowledgeState() {
        _acknowledgeState.value = AcknowledgeState.Idle
    }
}
