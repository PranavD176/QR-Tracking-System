package com.qrtracker.tracko.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qrtracker.tracko.MainActivity
import com.qrtracker.tracko.network.RetrofitClient
import com.qrtracker.tracko.network.models.DeviceTokenRequest
import com.qrtracker.tracko.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function fires when Firebase gives this device a new FCM token.
    // A token is like a unique address for this device — the backend needs it
    // to know WHERE to send the push notification.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save FCM token locally, then sync with backend if user is logged in.
        val tokenManager = TokenManager(applicationContext)
        tokenManager.saveFcmToken(token)
        if (!tokenManager.getToken().isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.registerDeviceToken(DeviceTokenRequest(token))
                } catch (_: Exception) {
                    // Best-effort background sync; next login/session refresh can retry.
                }
            }
        }
    }

    // This function fires when a push notification arrives on this device.
    // The backend sends this when a package is misplaced or a parcel event occurs.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract the notification title and body sent by the backend.
        // We always prefer the 'data' payload (guaranteed delivery even in Doze mode),
        // falling back to the 'notification' block if data keys are absent.
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Package Alert"
        val body  = remoteMessage.data["body"]  ?: remoteMessage.notification?.body  ?: "Your package was scanned."
        val eventId = remoteMessage.data["event_id"]

        // When the app is in the BACKGROUND, Android's FCM SDK automatically shows
        // the 'notification' block as a system tray notification — so we only need to
        // call showNotification() ourselves when the app is in the FOREGROUND (otherwise
        // the user would see two notifications).
        //
        // If the message is data-only (no notification block), we show it ourselves in all states.
        val isDataOnly = remoteMessage.notification == null
        if (isDataOnly || isAppInForeground()) {
            showNotification(title, body, eventId)
        }
    }

    // Builds and shows the actual notification the user sees on their screen
    private fun showNotification(title: String, body: String, eventId: String?) {
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
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .build()

        // Use event-scoped IDs so notifications don't overwrite each other.
        val notificationId = eventId?.hashCode()?.and(0x7fffffff)
            ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        notificationManager.notify(notificationId, notification)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = activityManager.runningAppProcesses ?: return false
        val packageName = applicationContext.packageName
        return processes.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName == packageName
        }
    }
}

