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
    @POST("auth/device-token")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Response<UpdatedResponse>

    // Protected — get all users for picking receiver/intermediates
    @GET("auth/users")
    suspend fun getUsers(
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<UserResponse>>>

    // ─── PACKAGE ENDPOINTS ───────────────────────────────────────────

    // Protected — returns packages where user is sender, receiver, or checkpoint
    @GET("packages")
    suspend fun getPackages(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<PackageResponse>>>

    // Protected — create a new package (any user)
    @POST("packages")
    suspend fun createPackage(
        @Body request: CreatePackageRequest
    ): Response<ApiResponse<PackageResponse>>

    // Protected — get full scan history for a specific package
    @GET("packages/{package_id}/scans")
    suspend fun getPackageScans(
        @Path("package_id") packageId: String
    ): Response<ApiResponse<List<ScanHistoryResponse>>>

    // Protected — receiver can accept a pending package
    @PUT("packages/{package_id}/accept")
    suspend fun acceptPackage(
        @Path("package_id") packageId: String
    ): Response<ApiResponse<PackageResponse>>

    // Protected — receiver can reject a pending package
    @PUT("packages/{package_id}/reject")
    suspend fun rejectPackage(
        @Path("package_id") packageId: String
    ): Response<ApiResponse<PackageResponse>>

    // Protected — sender can modify route checkpoints
    @PUT("packages/{package_id}/checkpoints")
    suspend fun updateCheckpoints(
        @Path("package_id") packageId: String,
        @Body checkpoints: Map<String, List<String>>
    ): Response<ApiResponse<PackageResponse>>

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
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<List<AlertResponse>>>

    // Protected — acknowledge (dismiss) an alert
    @PUT("alerts/{alert_id}/acknowledge")
    suspend fun acknowledgeAlert(
        @Path("alert_id") alertId: String
    ): Response<ApiResponse<AcknowledgeResponse>>

    @PUT("alerts/acknowledge-all")
    suspend fun acknowledgeAllAlerts(
        @Query("status") status: String? = "sent"
    ): Response<ApiResponse<BulkAcknowledgeResponse>>

    // ─── DASHBOARD ───────────────────────────────────────────────────

    // Protected — personal dashboard (scoped to current user)
    @GET("dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardResponse>>
}
