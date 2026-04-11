package com.qrtracker.tracko.ui.navigation

object Routes {

    // ── Auth ────────────────────────────────────────────────────────────────
    const val LOGIN    = "login"
    const val REGISTER = "register"

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
    const val ALERTS       = "alerts"
    const val ADMIN_ALERTS = "admin_alerts"

    // ── Helper functions to build routes with arguments ──────────────────────
    fun packageDetail(packageId: String)  = "package_detail/$packageId"

    fun scanResult(
        result: String,
        packageDesc: String,
        ownerName: String,
        alertSent: Boolean
    ) = "scan_result/$result/$packageDesc/$ownerName/$alertSent"
}
