package com.ganesh.qrtracker.local

import androidx.room.*
import com.ganesh.qrtracker.local.entities.PackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {

    // Insert or update a package — if same package_id exists, replace it
    @Upsert
    suspend fun upsertPackage(pkg: PackageEntity)

    // Insert a full list of packages at once — used when refreshing from API
    @Upsert
    suspend fun upsertAllPackages(packages: List<PackageEntity>)

    // Returns all packages as a Flow — UI updates automatically when data changes
    @Query("SELECT * FROM packages ORDER BY created_at DESC")
    fun getAllPackages(): Flow<List<PackageEntity>>

    // Returns only active packages
    @Query("SELECT * FROM packages WHERE status = 'active' ORDER BY created_at DESC")
    fun getActivePackages(): Flow<List<PackageEntity>>

    // Get a single package by ID
    @Query("SELECT * FROM packages WHERE package_id = :packageId")
    suspend fun getPackageById(packageId: String): PackageEntity?

    // Delete all packages — called on logout to clear local data
    @Query("DELETE FROM packages")
    suspend fun clearAllPackages()
}
