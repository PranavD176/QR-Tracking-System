package com.ganesh.qrtracker.utils

object QRParser {
    private const val PREFIX = "QR_TRACKING:"

    // Returns packageId if valid, null if not a valid QR_TRACKING code
    fun extractPackageId(rawValue: String): String? {
        if (!rawValue.startsWith(PREFIX)) return null
        val uuid = rawValue.removePrefix(PREFIX)
        if (uuid.length != 36) return null
        return uuid
    }
}