package com.qrtracker.tracko.network

import com.qrtracker.tracko.network.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── AUTH ENDPOINTS ──────────────────────────────────────────────

    // Public — no token needed
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    // Public — no token needed
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    // Protected — sends FCM token to backend after login
    // JWT is auto-attached by interceptor, no @Header needed
    @POST("auth/device-token")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Response<UpdatedResponse>

    // ─── PACKAGE ENDPOINTS ───────────────────────────────────────────

    // Protected — returns all packages owned by logged-in user
    @GET("packages")
    suspend fun getPackages(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<PackageResponse>>>

    // Protected — create a new package
    @POST("packages")
    suspend fun createPackage(
        @Body request: CreatePackageRequest
    ): Response<ApiResponse<PackageResponse>>

    // Protected — get full scan history for a specific package
    @GET("packages/{package_id}/scans")
    suspend fun getPackageScans(
        @Path("package_id") packageId: String
    ): Response<ApiResponse<List<ScanHistoryResponse>>>

    // ─── SCAN ENDPOINT ───────────────────────────────────────────────

    // Protected — core endpoint: validate QR scan, detect misplacement
    @POST("scan")
    suspend fun submitScan(
        @Body request: ScanRequest
    ): Response<ApiResponse<ScanResponse>>

    // ─── ALERT ENDPOINTS ─────────────────────────────────────────────

    // Protected — get alerts for logged-in user
    @GET("alerts")
    suspend fun getAlerts(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<AlertResponse>>>

    // Protected — acknowledge (dismiss) an alert
    @PUT("alerts/{alert_id}/acknowledge")
    suspend fun acknowledgeAlert(
        @Path("alert_id") alertId: String
    ): Response<ApiResponse<AcknowledgeResponse>>

    // ─── ADMIN ENDPOINTS ─────────────────────────────────────────────

    // Protected + Admin role only — get all unacknowledged alerts system-wide
    @GET("admin/alerts")
    suspend fun getAdminAlerts(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<AdminAlertResponse>>>

    // Protected + Admin role only — get all users
    @GET("admin/users")
    suspend fun getAdminUsers(): Response<ApiResponse<List<UserResponse>>>
}
