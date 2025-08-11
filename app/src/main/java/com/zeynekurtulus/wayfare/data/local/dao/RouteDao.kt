package com.zeynekurtulus.wayfare.data.local.dao

import androidx.room.*
import com.zeynekurtulus.wayfare.data.local.entities.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    
    @Query("SELECT * FROM routes WHERE userId = :userId ORDER BY downloadedAt DESC")
    suspend fun getUserRoutes(userId: String): List<RouteEntity>
    
    @Query("SELECT * FROM routes WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getUserRoutesFlow(userId: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE isDownloaded = 1 ORDER BY downloadedAt DESC")
    suspend fun getDownloadedRoutes(): List<RouteEntity>
    
    @Query("SELECT * FROM routes WHERE isDownloaded = 1 ORDER BY downloadedAt DESC")
    fun getDownloadedRoutesFlow(): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE isDownloaded = 1 AND userId = :userId ORDER BY downloadedAt DESC")
    suspend fun getDownloadedRoutesByUser(userId: String): List<RouteEntity>
    
    @Query("SELECT * FROM routes WHERE isDownloaded = 1 AND userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadedRoutesByUserFlow(userId: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE routeId = :routeId")
    suspend fun getRoute(routeId: String): RouteEntity?
    
    @Query("SELECT * FROM routes WHERE routeId = :routeId")
    fun getRouteFlow(routeId: String): Flow<RouteEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)
    
    @Update
    suspend fun updateRoute(route: RouteEntity)
    
    @Query("UPDATE routes SET isDownloaded = :isDownloaded, downloadedAt = :downloadedAt WHERE routeId = :routeId")
    suspend fun updateDownloadStatus(routeId: String, isDownloaded: Boolean, downloadedAt: Long?)
    
    @Query("DELETE FROM routes WHERE routeId = :routeId")
    suspend fun deleteRoute(routeId: String)
    
    @Query("DELETE FROM routes WHERE isDownloaded = 0 AND lastSyncedAt < :cutoffTime")
    suspend fun deleteOldCachedRoutes(cutoffTime: Long)
    
    @Query("UPDATE routes SET isDownloaded = 0, downloadedAt = NULL WHERE userId = :userId")
    suspend fun clearDownloadedRoutesForUser(userId: String)
    
    @Query("SELECT COUNT(*) FROM routes WHERE isDownloaded = 1")
    suspend fun getDownloadedRoutesCount(): Int
    
    @Query("SELECT COUNT(*) FROM routes WHERE userId = :userId")
    suspend fun getUserRoutesCount(userId: String): Int
    
    // Check if route exists locally
    @Query("SELECT COUNT(*) FROM routes WHERE routeId = :routeId")
    suspend fun routeExists(routeId: String): Int
    
    // Get routes that need syncing (older than specified time)
    @Query("SELECT * FROM routes WHERE lastSyncedAt < :cutoffTime OR lastSyncedAt IS NULL")
    suspend fun getRoutesNeedingSync(cutoffTime: Long): List<RouteEntity>
}