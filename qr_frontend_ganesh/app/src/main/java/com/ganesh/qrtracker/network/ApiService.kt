package com.ganesh.qrtracker.network

import com.ganesh.qrtracker.network.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("packages")
    suspend fun getPackages(@Header("Authorization") token: String): Response<List<PackageResponse>>

    @POST("packages")
    suspend fun createPackage(
        @Header("Authorization") token: String,
        @Body request: CreatePackageRequest
    ): Response<PackageResponse>

    @POST("scan")
    suspend fun submitScan(
        @Header("Authorization") token: String,
        @Body request: ScanRequest
    ): Response<ScanResponse>
}