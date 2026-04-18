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
    const val SCAN_RESULT = "scan_result/{result}/{packageDesc}/{ownerName}/{alertSent}"

    // ── Alerts ──────────────────────────────────────────────────────────────
    const val ALERTS       = "alerts"           // User-facing alert feed
    const val ADMIN_ALERTS = "admin_alerts"      // Operational admin alerts (bottom nav)
    const val APP_ALERTS   = "app_alerts"        // System notifications (bell icon)
    const val USER_ALERTS  = "user_alerts"       // User alert screen

    // ── Admin Checkpoint ────────────────────────────────────────────────────
    const val ADMIN_CHECKPOINT = "admin_checkpoint"
    const val ADMIN_PACKAGES   = "admin_packages"
    const val ADMIN_CREATE_PACKAGE = "admin_create_package"
    const val ADMIN_PROFILE    = "admin_profile"
    const val CHECKPOINT_PROFILE = "checkpoint_profile"

    // ── Admin Logistics Dashboard ─────────────────────────────────────────
    const val LOGISTICS_DASHBOARD = "logistics_dashboard/{packageId}"

    // ── Checkpoint Staff ──────────────────────────────────────────────────
    const val STAFF_HOME       = "staff_home"
    const val STAFF_SCAN       = "staff_scan"
    const val STAFF_SCAN_RESULT = "staff_scan_result/{orderId}/{status}/{currentCheckpoint}/{nextCheckpoint}"
    const val STAFF_HISTORY    = "staff_history"

    // ── Helper functions to build routes with arguments ──────────────────────
    fun packageDetail(packageId: String)  = "package_detail/$packageId"

    fun scanResult(
        result: String,
        packageDesc: String,
        ownerName: String,
        alertSent: Boolean
    ) = "scan_result/$result/$packageDesc/$ownerName/$alertSent"

    fun logisticsDashboard(packageId: String) = "logistics_dashboard/$packageId"

    fun staffScanResult(
        orderId: String,
        status: String,
        currentCheckpoint: String,
        nextCheckpoint: String
    ) = "staff_scan_result/$orderId/$status/$currentCheckpoint/$nextCheckpoint"
}
