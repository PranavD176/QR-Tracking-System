package com.ganesh.qrtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.qrtracker.network.RetrofitClient
import com.ganesh.qrtracker.network.models.DeviceTokenRequest
import com.ganesh.qrtracker.network.models.LoginRequest
import com.ganesh.qrtracker.network.models.RegisterRequest
import com.ganesh.qrtracker.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents every possible state the Login/Register screen can be in
sealed class AuthState {
    object Idle : AuthState()       // Nothing happening — initial state
    object Loading : AuthState()    // API call in progress — show spinner
    data class Success(
        val userId: String,
        val role: String
    ) : AuthState()                 // Login/Register succeeded
    data class Error(
        val message: String
    ) : AuthState()                 // Something went wrong — show error
}

class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    // The API service instance — built with our JWT interceptor
    private val apiService = RetrofitClient.getInstance(tokenManager)

    // _authState is private — only this ViewModel can change it
    // authState is public — screens observe this read-only version
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Called when user taps Login button
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val response = apiService.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    // Save token and role locally for future API calls
                    tokenManager.saveToken(data.token)
                    tokenManager.saveRole(data.role)
                    tokenManager.saveUserId(data.user_id)

                    // Send FCM token to backend immediately after login
                    sendFcmToken()

                    _authState.value = AuthState.Success(data.user_id, data.role)

                } else {
                    val errorMsg = response.body()?.error ?: "Login failed"
                    _authState.value = AuthState.Error(errorMsg)
                }

            } catch (e: Exception) {
                // Network error, timeout, etc.
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    // Called when user taps Register button
    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val response = apiService.register(
                    RegisterRequest(email, password, fullName)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    // After successful registration, log them in automatically
                    login(email, password)

                } else {
                    val errorMsg = response.body()?.error ?: "Registration failed"
                    _authState.value = AuthState.Error(errorMsg)
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    // Sends the FCM device token to backend so it knows where to send
    // push notifications for this user
    private fun sendFcmToken() {
        viewModelScope.launch {
            try {
                val fcmToken = tokenManager.getFcmToken() ?: return@launch
                apiService.registerDeviceToken(DeviceTokenRequest(fcmToken))
                // We don't update UI state here — this is a silent background call
            } catch (e: Exception) {
                // Silently fail — FCM token can be retried on next login
            }
        }
    }

    // Called when user logs out — clear all saved data
    fun logout() {
        tokenManager.clearAll()
        _authState.value = AuthState.Idle
    }

    // Reset state back to Idle — useful when navigating back to login screen
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}