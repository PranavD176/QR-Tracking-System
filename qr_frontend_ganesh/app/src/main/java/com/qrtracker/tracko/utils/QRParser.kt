package com.qrtracker.tracko.utils

object QRParser {
    private const val PREFIX = "QR_TRACKING:"

    // Returns packageId if valid, null if not a valid QR_TRACKING code
    fun extractPackageId(rawValue: String): String? {
        var uuid = rawValue.trim()
        
        if (!uuid.startsWith(PREFIX)) return null
        
        while (uuid.startsWith(PREFIX)) {
            uuid = uuid.removePrefix(PREFIX).trim()
        }
        
        // Return the extracted UUID (assuming valid package ID length is >= 1)
        if (uuid.isEmpty()) return null
        return uuid
    }
}
