package com.qrtracker.tracko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.ScanRequest
import com.qrtracker.tracko.network.models.ScanResponse
import com.qrtracker.tracko.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    // result = "valid" → green screen, "misplaced" → red screen
    data class Success(val scanResponse: ScanResponse) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    // Called by Member 1's ScanScreen after QR code is successfully read
    // packageId is extracted from "QR_TRACKING:<uuid>" by QRParser
    fun submitScan(packageId: String, locationDescription: String) {
        viewModelScope.launch {
            _scanState.value = ScanState.Loading

            try {
                val response = apiService.submitScan(
                    ScanRequest(packageId, locationDescription)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val scanResult = response.body()!!.data!!
                    _scanState.value = ScanState.Success(scanResult)

                } else {
                    if (response.code() == 401) {
                        _scanState.value = ScanState.Error("Session expired. Please login again.")
                    } else if (response.code() == 404) {
                        _scanState.value = ScanState.Error("Invalid QR code — package not found.")
                    } else {
                        val errorBodyString = response.errorBody()?.string()
                        val errorMsg = response.body()?.error ?: "Code: ${response.code()}, Body: $errorBodyString"
                        _scanState.value = ScanState.Error(errorMsg)
                    }
                }

            } catch (e: Exception) {
                _scanState.value = ScanState.Error("Network error: ${e.message}")
            }
        }
    }

    // Reset back to Idle — called when user navigates away from result screen
    fun resetScanState() {
        _scanState.value = ScanState.Idle
    }
}
