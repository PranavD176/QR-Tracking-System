package com.qrtracker.tracko.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(private val context: Context) {

    // MasterKey is used to encrypt/decrypt the storage
    // This ensures JWT token is stored securely on device
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = try {
        EncryptedSharedPreferences.create(
            context,
            "qrtracker_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If decryption fails (corrupted data or key mismatch), clear and recreate
        try {
            context.deleteSharedPreferences("qrtracker_secure_prefs")
        } catch (ignored: Exception) {
        }
        EncryptedSharedPreferences.create(
            context,
            "qrtracker_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ─── JWT TOKEN ───────────────────────────────────────────────────

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // ─── USER ID ─────────────────────────────────────────────────────

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // ─── FULL NAME ────────────────────────────────────────────────

    fun saveFullName(fullName: String) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply()
    }

    fun getFullName(): String? {
        return prefs.getString(KEY_FULL_NAME, null)
    }

    // ─── EMAIL ────────────────────────────────────────────────────

    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    // ─── ROLE ─────────────────────────────────────────────────────

    fun saveRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    // ─── FCM TOKEN ───────────────────────────────────────────────────
    // FCM token is stored in regular (non-encrypted) prefs
    // because it's not sensitive — it's just a device identifier

    fun saveFcmToken(token: String) {
        context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String? {
        return context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }

    // ─── LOGOUT ──────────────────────────────────────────────────────
    // Clears all saved auth data — called on logout

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ─── HELPERS ─────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_ROLE = "user_role"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
