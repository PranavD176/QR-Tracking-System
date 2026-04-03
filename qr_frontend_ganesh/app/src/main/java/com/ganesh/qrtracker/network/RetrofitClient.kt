package com.ganesh.qrtracker.network

import com.ganesh.qrtracker.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // During development we use 10.0.2.2 which points to your
    // computer's localhost from inside the Android emulator.
    // For physical device on same WiFi, Member 3 will provide their IP.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // This is the JWT interceptor — it runs before EVERY outgoing API request.
    // It reads the saved token from TokenManager and attaches it automatically.
    // This means ApiService never needs @Header("Authorization") on any call.
    private fun authInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // Read the saved JWT token
            val token = tokenManager.getToken()

            // If token exists, attach it. If not, send request as-is (for login/register)
            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }
    }

    // Logging interceptor — prints every request and response in Logcat.
    // Very useful during development to debug API calls.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Build the RetrofitClient with TokenManager injected
    // Call this once from Application class or ViewModel
    fun getInstance(tokenManager: TokenManager): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor(tokenManager)) // JWT auto-attach
            .addInterceptor(loggingInterceptor)            // Request/response logging
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}