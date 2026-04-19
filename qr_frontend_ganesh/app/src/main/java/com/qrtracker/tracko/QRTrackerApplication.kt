package com.qrtracker.tracko

import android.app.Application
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.utils.TokenManager

class QRTrackerApplication : Application() {
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager)
    }
}
