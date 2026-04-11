package com.qrtracker.tracko.network

import com.qrtracker.tracko.utils.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ── MOCK: replace with real IP before integration week ──────────────────
    // Emulator  → "http://10.0.2.2:8000/api/"
    // Device    → "http://<your-machine-ip>:8000/api/"
//    private const val BASE_URL = "http://10.0.2.2:8000/api/"
    private const val BASE_URL = "http://localhost:8000/api/"
    private var tokenManager: TokenManager? = null

    // Call this once from Application class before any API call
    fun init(tm: TokenManager) {
        tokenManager = tm
    }

    // ── JWT Interceptor ──────────────────────────────────────────────────────
    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager?.getToken()
        val request = chain.request().newBuilder().apply {
            if (token != null) addHeader("Authorization", "Bearer $token")
        }.build()

        val response = chain.proceed(request)

        // 401 → token expired or invalid → clear immediately
        if (response.code == 401) {
            tokenManager?.clearAll()
            // Member 2 will observe isLoggedIn state and redirect to Login
        }

        response
    }

    // ── Logging (DEBUG builds only — restrict in release when backend is ready) ──
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ── OkHttp Client ────────────────────────────────────────────────────────
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Gson ─────────────────────────────────────────────────────────────────
    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    // ── Retrofit Instance ────────────────────────────────────────────────────
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // ── API Service ───────────────────────────────────────────────────────────
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
