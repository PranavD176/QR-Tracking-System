package com.ganesh.qrtracker

import android.app.Application
import com.ganesh.qrtracker.network.RetrofitClient
import com.ganesh.qrtracker.utils.TokenManager

class QRTrackerApplication : Application() {
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager)
    }
}