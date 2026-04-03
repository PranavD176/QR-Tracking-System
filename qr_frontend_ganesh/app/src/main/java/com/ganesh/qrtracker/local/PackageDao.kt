package com.ganesh.qrtracker.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages")
    fun getAllPackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)

    @Query("DELETE FROM packages")
    suspend fun clearAll()
}
