package com.qrtracker.tracko.ui.navigation

object Routes {

    // ── Auth ────────────────────────────────────────────────────────────────
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val USER_PROFILE = "user_profile"

    // ── Main ────────────────────────────────────────────────────────────────
    const val HOME     = "home"

    // ── Packages ────────────────────────────────────────────────────────────
    const val PACKAGE_LIST   = "package_list"
    const val PACKAGE_DETAIL = "package_detail/{packageId}"
    const val CREATE_PACKAGE = "create_package"

    // ── Scanner ─────────────────────────────────────────────────────────────
    const val SCANNER    = "scanner"
    const val SCAN_RESULT = "scan_result/{result}/{packageDesc}/{senderName}/{alertSent}"

    // ── Alerts ──────────────────────────────────────────────────────────────
    const val ALERTS       = "alerts"           // User-facing alert feed
    const val APP_ALERTS   = "app_alerts"        // System notifications (bell icon)

    // ── Logistics Dashboard ─────────────────────────────────────────────────
    const val LOGISTICS_DASHBOARD = "logistics_dashboard/{packageId}"

    // ── Helper functions to build routes with arguments ──────────────────────
    fun packageDetail(packageId: String)  = "package_detail/$packageId"

    fun scanResult(
        result: String,
        packageDesc: String,
        senderName: String?,
        alertSent: Boolean
    ): String {
        val safeSender = senderName ?: "Unknown"
        return "scan_result/$result/$packageDesc/$safeSender/$alertSent"
    }

    fun logisticsDashboard(packageId: String) = "logistics_dashboard/$packageId"
}
