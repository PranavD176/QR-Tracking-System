package com.ganesh.qrtracker.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "qrtracker_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ROLE  = "user_role"
        private const val KEY_UID   = "user_id"
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?      = prefs.getString(KEY_TOKEN, null)

    fun saveRole(role: String)   = prefs.edit().putString(KEY_ROLE, role).apply()
    fun getRole(): String?       = prefs.getString(KEY_ROLE, null)

    fun saveUserId(uid: String)  = prefs.edit().putString(KEY_UID, uid).apply()
    fun getUserId(): String?     = prefs.getString(KEY_UID, null)

    fun isLoggedIn(): Boolean    = getToken() != null

    fun clearAll()               = prefs.edit().clear().apply()
}