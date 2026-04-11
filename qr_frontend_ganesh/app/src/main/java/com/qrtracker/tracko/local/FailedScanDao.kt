package com.qrtracker.tracko.local

import androidx.room.*
import com.qrtracker.tracko.local.entities.FailedScanEntity

@Dao
interface FailedScanDao {

    // Save a failed scan to retry later
    @Insert
    suspend fun insertFailedScan(scan: FailedScanEntity)

    // Get all pending failed scans
    @Query("SELECT * FROM failed_scans ORDER BY attempted_at ASC")
    suspend fun getAllFailedScans(): List<FailedScanEntity>

    // Delete a failed scan after it has been successfully retried
    @Query("DELETE FROM failed_scans WHERE id = :id")
    suspend fun deleteFailedScan(id: Int)

    // Delete all failed scans — called on logout
    @Query("DELETE FROM failed_scans")
    suspend fun clearAllFailedScans()
}
