package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    
    suspend fun createRoute(createRoute: CreateRoute): ApiResult<String>
    
    suspend fun getUserRoutes(): ApiResult<List<Route>>
    
    suspend fun getRoute(routeId: String): ApiResult<RouteDetail>
    
    suspend fun updateRoute(routeId: String, updateRoute: UpdateRoute): ApiResult<Unit>
    
    suspend fun deleteRoute(routeId: String): ApiResult<Unit>
    
    suspend fun getPublicRoutes(
        category: String? = null,
        season: String? = null,
        budget: String? = null,
        limit: Int = 10
    ): ApiResult<List<Route>>
    
    // ⭐ NEW: Privacy toggle for routes
    suspend fun toggleRoutePrivacy(routeId: String, isPublic: Boolean): ApiResult<Unit>
    
    // ⭐ NEW: Advanced public route search
    suspend fun searchPublicRoutes(searchParams: RouteSearchParams): ApiResult<List<Route>>
    
    // ⭐ OFFLINE FUNCTIONALITY
    
    /**
     * Download a route for offline access
     */
    suspend fun downloadRoute(routeId: String): ApiResult<Unit>
    
    /**
     * Remove downloaded route from local storage
     */
    suspend fun removeDownloadedRoute(routeId: String): ApiResult<Unit>
    
    /**
     * Get all downloaded routes for offline access
     */
    suspend fun getDownloadedRoutes(): ApiResult<List<Route>>
    
    /**
     * Get downloaded routes for the current user
     */
    suspend fun getDownloadedRoutesForCurrentUser(): ApiResult<List<Route>>
    
    /**
     * Get downloaded routes as Flow for reactive UI
     */
    fun getDownloadedRoutesFlow(): Flow<List<Route>>
    
    /**
     * Get downloaded routes for the current user as Flow for reactive UI
     */
    fun getDownloadedRoutesForCurrentUserFlow(): Flow<List<Route>>
    
    /**
     * Check if a route is downloaded for offline use
     */
    suspend fun isRouteDownloaded(routeId: String): Boolean
    
    /**
     * Get user routes from cache (offline-first)
     */
    suspend fun getUserRoutesOffline(): ApiResult<List<Route>>
    
    /**
     * Get route details from cache (offline-first)
     */
    suspend fun getRouteOffline(routeId: String): ApiResult<RouteDetail>
    
    /**
     * Sync local cache with server data
     */
    suspend fun syncWithServer(): ApiResult<Unit>
    
    /**
     * Clear old cached data
     */
    suspend fun clearOldCache(olderThanDays: Int = 7): ApiResult<Unit>
    
    /**
     * Clear downloaded routes for the current user (used during logout)
     */
    suspend fun clearDownloadedRoutesForCurrentUser(): ApiResult<Unit>
    
    /**
     * Clean up downloaded routes that don't belong to the current user
     * This can be called during app startup to ensure data integrity
     */
    suspend fun cleanupOrphanedDownloads(): ApiResult<Unit>
}