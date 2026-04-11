package com.qrtracker.tracko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.DeviceTokenRequest
import com.qrtracker.tracko.network.models.LoginRequest
import com.qrtracker.tracko.network.models.RegisterRequest
import com.qrtracker.tracko.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String, val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.login(LoginRequest(email, password))
                val body = response.body()
                
                if (response.isSuccessful && body != null) {
                    if (body.success && body.data != null) {
                        val authData = body.data
                        // Save token and role locally for future API calls
                        tokenManager.saveToken(authData.token)
                        tokenManager.saveRole(authData.role)
                        tokenManager.saveUserId(authData.user_id)

                        // Send FCM token to backend
                        sendFcmToken()

                        _authState.value = AuthState.Success(authData.user_id, authData.role)
                    } else {
                        _authState.value = AuthState.Error(body.error ?: "Login failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Login failed: Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.register(
                    RegisterRequest(fullName, email, password, role)
                )
                if (response.isSuccessful) {
                    login(email, password)
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    private fun sendFcmToken() {
        viewModelScope.launch {
            try {
                val fcmToken = tokenManager.getFcmToken() ?: return@launch
                apiService.registerDeviceToken(DeviceTokenRequest(fcmToken))
            } catch (e: Exception) {
                // Background task failed
            }
        }
    }

    fun logout() {
        tokenManager.clearAll()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

