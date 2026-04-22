package com.qrtracker.tracko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.AlertResponse
import com.qrtracker.tracko.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AlertListState {
    object Idle : AlertListState()
    object Loading : AlertListState()
    data class Success(val alerts: List<AlertResponse>) : AlertListState()
    data class Error(val message: String) : AlertListState()
}

sealed class AcknowledgeState {
    object Idle : AcknowledgeState()
    object Loading : AcknowledgeState()
    data class SuccessSingle(val alertId: String) : AcknowledgeState()
    data class SuccessBulk(val updated: Int) : AcknowledgeState()
    data class Error(val message: String) : AcknowledgeState()
}

class AlertViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // ─── ALERT LIST STATE ────────────────────────────────────────────

    private val _alertListState = MutableStateFlow<AlertListState>(AlertListState.Idle)
    val alertListState: StateFlow<AlertListState> = _alertListState

    // ─── ACKNOWLEDGE STATE ───────────────────────────────────────────

    private val _acknowledgeState = MutableStateFlow<AcknowledgeState>(AcknowledgeState.Idle)
    val acknowledgeState: StateFlow<AcknowledgeState> = _acknowledgeState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    // ─── FETCH MY ALERTS ─────────────────────────────────────────────

    fun fetchAlerts(status: String? = "sent") {
        viewModelScope.launch {
            _alertListState.value = AlertListState.Loading

            try {
                val response = apiService.getAlerts(status = status)

                if (response.isSuccessful && response.body()?.success == true) {
                    val alerts = response.body()!!.data ?: emptyList()
                    _alertListState.value = AlertListState.Success(alerts)
                    _unreadCount.value = alerts.count { it.status.equals("sent", ignoreCase = true) }

                } else {
                    val errorMsg = response.body()?.error ?: "Failed to fetch alerts"
                    _alertListState.value = AlertListState.Error(errorMsg)
                    _unreadCount.value = 0
                }

            } catch (e: Exception) {
                _alertListState.value = AlertListState.Error("Network error: ${e.message}")
                _unreadCount.value = 0
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
                    _acknowledgeState.value = AcknowledgeState.SuccessSingle(alertId)
                    fetchAlerts(null)

                } else {
                    val errorMsg = response.body()?.error ?: "Failed to acknowledge alert"
                    _acknowledgeState.value = AcknowledgeState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _acknowledgeState.value = AcknowledgeState.Error("Network error: ${e.message}")
            }
        }
    }

    fun acknowledgeAllAlerts() {
        viewModelScope.launch {
            _acknowledgeState.value = AcknowledgeState.Loading

            try {
                val response = apiService.acknowledgeAllAlerts(status = "sent")

                if (response.isSuccessful && response.body()?.success == true) {
                    val updated = response.body()?.data?.updated ?: 0
                    _acknowledgeState.value = AcknowledgeState.SuccessBulk(updated)
                    _unreadCount.value = 0
                    fetchAlerts(null)
                } else {
                    val errorMsg = response.body()?.error ?: "Failed to mark alerts as read"
                    _acknowledgeState.value = AcknowledgeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _acknowledgeState.value = AcknowledgeState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetAcknowledgeState() {
        _acknowledgeState.value = AcknowledgeState.Idle
    }
}
