package com.qrtracker.tracko.network.models

data class DashboardResponse(
    val stats: DashboardStats,
    val recent_scans: List<DashboardRecentScan>
)

data class DashboardStats(
    val total: Int,
    val received: Int,
    val misplaced: Int,
    val duplicate: Int
)

data class DashboardRecentScan(
    val parcel_id: String,
    val timestamp: String,
    val status: String
)
