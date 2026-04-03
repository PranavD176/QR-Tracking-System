package com.ganesh.qrtracker.network

import com.ganesh.qrtracker.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // JWT Auth Interceptor — attaches token to every outgoing request
    private fun authInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()

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

    // 401 Interceptor — reacts when server says token is expired/invalid
    // Clears all saved auth data so the app knows to redirect to login
    private fun unauthorizedInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // If server returns 401, clear token immediately
            // The ViewModel's error state will trigger the UI redirect
            if (response.code == 401) {
                tokenManager.clearAll()
            }

            response  // Always return the response so ViewModel can handle it
        }
    }

    // Logging interceptor — prints full request/response in Logcat
    // Very helpful during development and integration week
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun getInstance(tokenManager: TokenManager): ApiService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor(tokenManager))         // 1. Attach token
            .addInterceptor(unauthorizedInterceptor(tokenManager)) // 2. Handle 401
            .addInterceptor(loggingInterceptor)                    // 3. Log everything
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}