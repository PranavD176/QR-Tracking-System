package com.qrtracker.tracko.network.models

data class DashboardResponse(
    val stats: DashboardStats,
    val recent_scans: List<DashboardRecentScan>
)

data class DashboardStats(
    val sent: Int,
    val received: Int,
    val in_transit: Int,
    val delivered: Int
)

data class DashboardRecentScan(
    val parcel_id: String,
    val timestamp: String,
    val status: String
)
