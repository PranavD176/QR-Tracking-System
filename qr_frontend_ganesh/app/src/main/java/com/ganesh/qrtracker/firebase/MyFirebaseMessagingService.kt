package com.ganesh.qrtracker.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ganesh.qrtracker.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function fires when Firebase gives this device a new FCM token.
    // A token is like a unique address for this device — the backend needs it
    // to know WHERE to send the push notification.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save the token locally so we can send it to backend after login
        saveTokenLocally(token)
    }

    // This function fires when a push notification arrives on this device.
    // The backend sends this when a package is misplaced.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract the notification title and body sent by the backend
        val title = remoteMessage.notification?.title ?: "Package Alert"
        val body = remoteMessage.notification?.body ?: "Your package was scanned."

        // Build and display the notification to the user
        showNotification(title, body)
    }

    // Saves the FCM token in SharedPreferences so TokenManager can read it later
    private fun saveTokenLocally(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    // Builds and shows the actual notification the user sees on their screen
    private fun showNotification(title: String, body: String) {
        val channelId = "qr_tracker_alerts"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8+ requires a notification channel to be created first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Package Alerts",           // Name shown in phone settings
                NotificationManager.IMPORTANCE_HIGH  // Makes it a heads-up notification
            ).apply {
                description = "Alerts for misplaced packages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // This intent opens the app when the user taps the notification
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification appearance
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Temporary icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)   // Dismisses notification when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        // Display it — using a fixed ID (1) for all package alerts
        notificationManager.notify(1, notification)
    }
}