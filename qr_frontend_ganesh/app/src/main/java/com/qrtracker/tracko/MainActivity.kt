package com.qrtracker.tracko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.qrtracker.tracko.ui.navigation.NavGraph
import com.qrtracker.tracko.ui.theme.QRTrackerTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure we always have an FCM token at startup
        val tokenManager = com.qrtracker.tracko.utils.TokenManager(applicationContext)
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        tokenManager.saveFcmToken(token)
                    }
                }
            }
        } catch (e: Exception) {
            // Firebase not initialized - usually due to missing google-services.json
            android.util.Log.e("MainActivity", "Firebase initialization failed: ${e.message}")
        }
        
        enableEdgeToEdge()
        setContent {
            QRTrackerTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
