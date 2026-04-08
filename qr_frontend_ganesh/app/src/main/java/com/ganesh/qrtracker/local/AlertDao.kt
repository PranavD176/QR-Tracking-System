package com.ganesh.qrtracker.local

import androidx.room.*
import com.ganesh.qrtracker.local.entities.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    // Insert or update an alert
    @Upsert
    suspend fun upsertAlert(alert: AlertEntity)

    // Insert a full list of alerts at once
    @Upsert
    suspend fun upsertAllAlerts(alerts: List<AlertEntity>)

    // Returns all unacknowledged alerts as a Flow
    @Query("SELECT * FROM alerts WHERE status = 'sent' ORDER BY created_at DESC")
    fun getActiveAlerts(): Flow<List<AlertEntity>>

    // Returns all alerts regardless of status
    @Query("SELECT * FROM alerts ORDER BY created_at DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    // Mark a single alert as acknowledged locally
    @Query("UPDATE alerts SET status = 'acknowledged' WHERE alert_id = :alertId")
    suspend fun acknowledgeAlert(alertId: String)

    // Delete all alerts — called on logout
    @Query("DELETE FROM alerts")
    suspend fun clearAllAlerts()
}