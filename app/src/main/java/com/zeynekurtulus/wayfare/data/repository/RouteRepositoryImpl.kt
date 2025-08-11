package com.zeynekurtulus.wayfare.data.repository

import android.util.Log
import com.zeynekurtulus.wayfare.data.api.services.RouteApiService
import com.zeynekurtulus.wayfare.data.local.dao.RouteDao
import com.zeynekurtulus.wayfare.data.mappers.RouteMapper
import com.zeynekurtulus.wayfare.data.mappers.RouteEntityMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.NetworkConnectivityManager
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class RouteRepositoryImpl(
    private val routeApiService: RouteApiService,
    private val routeDao: RouteDao,
    private val networkConnectivityManager: NetworkConnectivityManager,
    private val sharedPreferencesManager: SharedPreferencesManager
) : RouteRepository {
    
    override suspend fun createRoute(createRoute: CreateRoute): ApiResult<String> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = RouteMapper.mapToCreateRouteRequest(createRoute)
            val response = routeApiService.createRoute("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { createRouteResponse ->
                    ApiResult.Success(createRouteResponse.routeId)
                } ?: ApiResult.Error("Failed to create route")
            } else {
                ApiResult.Error("Failed to create route", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }

    override suspend fun getUserRoutes(): ApiResult<List<Route>> {
        return try {
            Log.d("RouteRepositoryImpl", "🚀 getUserRoutes() STARTED")
            
            val token = sharedPreferencesManager.getAccessToken()
            if (token == null) {
                Log.e("RouteRepositoryImpl", "Token bulunamadı!")
                return ApiResult.Error("User not authenticated")
            }
            Log.d("RouteRepositoryImpl", "Token found: ${token.take(10)}...")

            Log.d("RouteRepositoryImpl", "Making API call...")
            val response = routeApiService.getUserRoutes("Bearer $token")
            Log.d("RouteRepositoryImpl", "API yanıt kodu: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { userRoutesResponse ->
                    Log.d("RouteRepositoryImpl", "Response received: success=${userRoutesResponse.success}, message=${userRoutesResponse.message}")
                    Log.d("RouteRepositoryImpl", "Data array size: ${userRoutesResponse.data.size}")
                    
                    try {
                        val routes = userRoutesResponse.data.mapIndexed { index, routeDto ->
                            Log.d("RouteRepositoryImpl", "🔄 Mapping route $index: ${routeDto.title}")
                            Log.d("RouteRepositoryImpl", "   cityId: ${routeDto.cityId}")
                            Log.d("RouteRepositoryImpl", "   countryId: ${routeDto.countryId}")
                            Log.d("RouteRepositoryImpl", "   dates: ${routeDto.startDate} to ${routeDto.endDate}")
                            RouteMapper.mapToRoute(routeDto)
                        }
                        
                        // Cache the routes for offline access
                        try {
                            val entities = routes.map { route ->
                                // Check if route was previously downloaded to preserve download status
                                val existingEntity = routeDao.getRoute(route.routeId)
                                val isDownloaded = existingEntity?.isDownloaded ?: false
                                RouteEntityMapper.fromRoute(route, isDownloaded)
                            }
                            routeDao.insertRoutes(entities)
                            Log.d("RouteRepositoryImpl", "💾 Cached ${entities.size} routes locally")
                        } catch (cacheException: Exception) {
                            Log.w("RouteRepositoryImpl", "⚠️ Failed to cache routes: ${cacheException.message}")
                            // Continue even if caching fails
                        }
                        
                        Log.d("RouteRepositoryImpl", "✅ Successfully mapped ${routes.size} routes")
                        Log.d("RouteRepositoryImpl", "🎯 getUserRoutes() COMPLETED SUCCESSFULLY")
                        ApiResult.Success(routes)
                    } catch (mappingException: Exception) {
                        Log.e("RouteRepositoryImpl", "❌ Error during mapping: ${mappingException.message}", mappingException)
                        Log.e("RouteRepositoryImpl", "❌ Stack trace: ${mappingException.stackTraceToString()}")
                        ApiResult.Error("Error processing route data: ${mappingException.message}")
                    }
                } ?: run {
                    Log.e("RouteRepositoryImpl", "Yanıt gövdesi null!")
                    ApiResult.Error("Failed to get user routes")
                }
            } else {
                Log.e("RouteRepositoryImpl", "API başarısız: ${response.code()} - ${response.message()}")
                Log.e("RouteRepositoryImpl", "Response was not successful")
                ApiResult.Error("Failed to get user routes: ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "Exception in getUserRoutes: ${e.message}", e)
            Log.e("RouteRepositoryImpl", "Exception type: ${e.javaClass.simpleName}")
            ApiResult.Error("Network error: ${e.message ?: "Unknown error"}")
        }
    }
    
    override suspend fun getRoute(routeId: String): ApiResult<RouteDetail> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = routeApiService.getRoute("Bearer $token", routeId)
            
            if (response.isSuccessful) {
                response.body()?.let { singleRouteResponse ->
                    val routeDetail = RouteMapper.mapToRouteDetail(singleRouteResponse.data)
                    
                    // Cache the route detail for offline access
                    try {
                        val existingEntity = routeDao.getRoute(routeId)
                        val isDownloaded = existingEntity?.isDownloaded ?: false
                        val routeEntity = RouteEntityMapper.fromRouteDetail(routeDetail, isDownloaded)
                        routeDao.insertRoute(routeEntity)
                        Log.d("RouteRepositoryImpl", "💾 Cached route detail: $routeId")
                    } catch (cacheException: Exception) {
                        Log.w("RouteRepositoryImpl", "⚠️ Failed to cache route detail: ${cacheException.message}")
                        // Continue even if caching fails
                    }
                    
                    ApiResult.Success(routeDetail)
                } ?: ApiResult.Error("Failed to get route details")
            } else {
                ApiResult.Error("Route not found", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun updateRoute(routeId: String, updateRoute: UpdateRoute): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = RouteMapper.mapToUpdateRouteRequest(updateRoute)
            val response = routeApiService.updateRoute("Bearer $token", routeId, request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to update route", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun deleteRoute(routeId: String): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = routeApiService.deleteRoute("Bearer $token", routeId)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete route", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getPublicRoutes(
        category: String?,
        season: String?,
        budget: String?,
        limit: Int
    ): ApiResult<List<Route>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = routeApiService.getPublicRoutes(
                authorization = "Bearer $token",
                category = category,
                season = season,
                budget = budget,
                limit = limit
            )
            
            if (response.isSuccessful) {
                response.body()?.let { publicRoutesResponse ->
                    val routes = publicRoutesResponse.data.map { RouteMapper.mapToRoute(it) }
                    ApiResult.Success(routes)
                } ?: ApiResult.Error("Failed to get public routes")
            } else {
                ApiResult.Error("Failed to get public routes", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    // Privacy toggle implementation
    override suspend fun toggleRoutePrivacy(routeId: String, isPublic: Boolean): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = routeApiService.toggleRoutePrivacy(
                authorization = "Bearer $token",
                routeId = routeId,
                isPublic = isPublic
            )
            
            if (response.isSuccessful) {
                Log.d("RouteRepositoryImpl", "✅ Route privacy toggled: $routeId -> isPublic: $isPublic")
                ApiResult.Success(Unit)
            } else {
                Log.e("RouteRepositoryImpl", "❌ Failed to toggle route privacy: ${response.code()}")
                ApiResult.Error("Failed to toggle route privacy", response.code())
            }
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception toggling route privacy: ${e.message}", e)
            NetworkUtils.handleApiError(e)
        }
    }
    
    //Advanced public route search implementation
    override suspend fun searchPublicRoutes(searchParams: RouteSearchParams): ApiResult<List<Route>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = routeApiService.searchPublicRoutes(
                authorization = "Bearer $token",
                searchQuery = searchParams.q,
                city = searchParams.city,
                country = searchParams.country,
                category = searchParams.category,
                season = searchParams.season,
                budget = searchParams.budget,
                travelStyle = searchParams.travelStyle,
                limit = searchParams.limit,
                sortBy = searchParams.sortBy
            )
            
            if (response.isSuccessful) {
                response.body()?.let { searchResponse ->
                    val routes = searchResponse.data.map { RouteMapper.mapToRoute(it) }
                    Log.d("RouteRepositoryImpl", "✅ Found ${routes.size} public routes matching search criteria")
                    ApiResult.Success(routes)
                } ?: ApiResult.Error("Failed to search public routes")
            } else {
                Log.e("RouteRepositoryImpl", "❌ Failed to search public routes: ${response.code()}")
                ApiResult.Error("Failed to search public routes", response.code())
            }
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception searching public routes: ${e.message}", e)
            NetworkUtils.handleApiError(e)
        }
    }
    
    // OFFLINE FUNCTIONALITY IMPLEMENTATION
    
    override suspend fun downloadRoute(routeId: String): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🔽 Downloading route: $routeId")
            
            // First check if we have network connectivity
            val isNetworkAvailable = networkConnectivityManager.isNetworkAvailable()
            Log.d("RouteRepositoryImpl", "🌐 Network availability check: $isNetworkAvailable")
            
            if (!isNetworkAvailable) {
                Log.w("RouteRepositoryImpl", "❌ Network not available, cannot download route")
                return ApiResult.Error("No network connection available")
            }
            
            // Fetch the complete route details from API
            when (val result = getRoute(routeId)) {
                is ApiResult.Success -> {
                    val routeDetail = result.data
                    val routeEntity = RouteEntityMapper.fromRouteDetail(routeDetail, isDownloaded = true)
                    
                    // Save to local database
                    routeDao.insertRoute(routeEntity)
                    
                    Log.d("RouteRepositoryImpl", "✅ Route downloaded successfully: $routeId")
                    ApiResult.Success(Unit)
                }
                is ApiResult.Error -> {
                    Log.e("RouteRepositoryImpl", "❌ Failed to fetch route for download: ${result.message}")
                    ApiResult.Error("Failed to download route: ${result.message}")
                }
                else -> ApiResult.Error("Failed to download route")
            }
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception downloading route: ${e.message}", e)
            ApiResult.Error("Failed to download route: ${e.message}")
        }
    }
    
    override suspend fun removeDownloadedRoute(routeId: String): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🗑️ Removing downloaded route: $routeId")
            
            // Update the download status instead of deleting completely
            routeDao.updateDownloadStatus(routeId, isDownloaded = false, downloadedAt = null)
            
            Log.d("RouteRepositoryImpl", "✅ Route download removed: $routeId")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception removing downloaded route: ${e.message}", e)
            ApiResult.Error("Failed to remove downloaded route: ${e.message}")
        }
    }
    
    override suspend fun getDownloadedRoutes(): ApiResult<List<Route>> {
        return try {
            Log.d("RouteRepositoryImpl", "📱 Getting downloaded routes from local storage")
            
            val routeEntities = routeDao.getDownloadedRoutes()
            val routes = routeEntities.map { RouteEntityMapper.toRoute(it) }
            
            Log.d("RouteRepositoryImpl", "✅ Found ${routes.size} downloaded routes")
            ApiResult.Success(routes)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception getting downloaded routes: ${e.message}", e)
            ApiResult.Error("Failed to get downloaded routes: ${e.message}")
        }
    }
    
    override suspend fun getDownloadedRoutesForCurrentUser(): ApiResult<List<Route>> {
        return try {
            Log.d("RouteRepositoryImpl", "📱 Getting downloaded routes for current user")
            
            val userId = sharedPreferencesManager.getUserId()
                ?: return ApiResult.Error("User not authenticated")
            
            val routeEntities = routeDao.getDownloadedRoutesByUser(userId)
            val routes = routeEntities.map { RouteEntityMapper.toRoute(it) }
            
            Log.d("RouteRepositoryImpl", "✅ Found ${routes.size} downloaded routes for user: $userId")
            ApiResult.Success(routes)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception getting downloaded routes for current user: ${e.message}", e)
            ApiResult.Error("Failed to get downloaded routes: ${e.message}")
        }
    }
    
    override fun getDownloadedRoutesFlow(): Flow<List<Route>> {
        return routeDao.getDownloadedRoutesFlow()
            .map { entities -> entities.map { RouteEntityMapper.toRoute(it) } }
    }
    
    override fun getDownloadedRoutesForCurrentUserFlow(): Flow<List<Route>> {
        val userId = sharedPreferencesManager.getUserId()
        return if (userId != null) {
            routeDao.getDownloadedRoutesByUserFlow(userId)
                .map { entities -> entities.map { RouteEntityMapper.toRoute(it) } }
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    
    override suspend fun isRouteDownloaded(routeId: String): Boolean {
        return try {
            val entity = routeDao.getRoute(routeId)
            entity?.isDownloaded == true
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception checking download status: ${e.message}", e)
            false
        }
    }
    
    override suspend fun getUserRoutesOffline(): ApiResult<List<Route>> {
        return try {
            Log.d("RouteRepositoryImpl", "📱 Getting user routes (offline-first)")
            
            val userId = sharedPreferencesManager.getUserId()
                ?: return ApiResult.Error("User not authenticated")
            
            // Try to get from cache first
            val cachedEntities = routeDao.getUserRoutes(userId)
            if (cachedEntities.isNotEmpty()) {
                val routes = cachedEntities.map { RouteEntityMapper.toRoute(it) }
                Log.d("RouteRepositoryImpl", "✅ Found ${routes.size} routes in cache")
                
                // If we have network, sync in background but return cached data immediately
                if (networkConnectivityManager.isNetworkAvailable()) {
                    // Background sync could be triggered here
                    Log.d("RouteRepositoryImpl", "🔄 Network available, cache data returned immediately")
                }
                
                return ApiResult.Success(routes)
            }
            
            // If no cache and we have network, fetch from API
            if (networkConnectivityManager.isNetworkAvailable()) {
                Log.d("RouteRepositoryImpl", "🌐 No cache found, fetching from network")
                return getUserRoutes() // This will also cache the results
            }
            
            // No cache and no network
            Log.d("RouteRepositoryImpl", "❌ No cache and no network connection")
            ApiResult.Error("No routes available offline")
            
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception getting user routes offline: ${e.message}", e)
            ApiResult.Error("Failed to get routes: ${e.message}")
        }
    }
    
    override suspend fun getRouteOffline(routeId: String): ApiResult<RouteDetail> {
        return try {
            Log.d("RouteRepositoryImpl", "📱 Getting route offline: $routeId")
            
            // Try cache first
            val cachedEntity = routeDao.getRoute(routeId)
            if (cachedEntity != null) {
                val routeDetail = RouteEntityMapper.toRouteDetail(cachedEntity)
                Log.d("RouteRepositoryImpl", "✅ Found route in cache: $routeId")
                return ApiResult.Success(routeDetail)
            }
            
            // If no cache and we have network, fetch from API
            if (networkConnectivityManager.isNetworkAvailable()) {
                Log.d("RouteRepositoryImpl", "🌐 Route not in cache, fetching from network")
                return getRoute(routeId) // This will also cache the result
            }
            
            // No cache and no network
            Log.d("RouteRepositoryImpl", "❌ Route not available offline: $routeId")
            ApiResult.Error("Route not available offline")
            
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception getting route offline: ${e.message}", e)
            ApiResult.Error("Failed to get route: ${e.message}")
        }
    }
    
    override suspend fun syncWithServer(): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🔄 Syncing with server")
            
            if (!networkConnectivityManager.isNetworkAvailable()) {
                return ApiResult.Error("No network connection available")
            }
            
            val userId = sharedPreferencesManager.getUserId()
                ?: return ApiResult.Error("User not authenticated")
            
            // Fetch latest routes from server
            when (val result = getUserRoutes()) {
                is ApiResult.Success -> {
                    val routes = result.data
                    
                    // Update cache with fresh data
                    val entities = routes.map { route ->
                        // Check if route was previously downloaded
                        val existingEntity = routeDao.getRoute(route.routeId)
                        val isDownloaded = existingEntity?.isDownloaded ?: false
                        RouteEntityMapper.fromRoute(route, isDownloaded)
                    }
                    
                    routeDao.insertRoutes(entities)
                    
                    Log.d("RouteRepositoryImpl", "✅ Sync completed: ${routes.size} routes updated")
                    ApiResult.Success(Unit)
                }
                is ApiResult.Error -> {
                    Log.e("RouteRepositoryImpl", "❌ Sync failed: ${result.message}")
                    ApiResult.Error("Sync failed: ${result.message}")
                }
                else -> ApiResult.Error("Sync failed")
            }
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception during sync: ${e.message}", e)
            ApiResult.Error("Sync failed: ${e.message}")
        }
    }
    
    override suspend fun clearOldCache(olderThanDays: Int): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🧹 Clearing cache older than $olderThanDays days")
            
            val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(olderThanDays.toLong())
            routeDao.deleteOldCachedRoutes(cutoffTime)
            
            Log.d("RouteRepositoryImpl", "✅ Old cache cleared")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception clearing cache: ${e.message}", e)
            ApiResult.Error("Failed to clear cache: ${e.message}")
        }
    }
    
    override suspend fun clearDownloadedRoutesForCurrentUser(): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🧹 Clearing downloaded routes for current user")
            
            val userId = sharedPreferencesManager.getUserId()
                ?: return ApiResult.Error("User not authenticated")
            
            routeDao.clearDownloadedRoutesForUser(userId)
            
            Log.d("RouteRepositoryImpl", "✅ Downloaded routes cleared for user: $userId")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception clearing downloaded routes: ${e.message}", e)
            ApiResult.Error("Failed to clear downloaded routes: ${e.message}")
        }
    }
    
    /**
     * Clean up downloaded routes that don't belong to the current user
     * This can be called during app startup to ensure data integrity
     */
    override suspend fun cleanupOrphanedDownloads(): ApiResult<Unit> {
        return try {
            Log.d("RouteRepositoryImpl", "🧹 Cleaning up orphaned downloads")
            
            val userId = sharedPreferencesManager.getUserId()
                ?: return ApiResult.Error("User not authenticated")
            
            // Get all downloaded routes
            val allDownloaded = routeDao.getDownloadedRoutes()
            
            // Find routes that don't belong to current user
            val orphanedRoutes = allDownloaded.filter { it.userId != userId }
            
            if (orphanedRoutes.isNotEmpty()) {
                Log.d("RouteRepositoryImpl", "Found ${orphanedRoutes.size} orphaned downloads, clearing them")
                
                // Clear orphaned downloads
                orphanedRoutes.forEach { route ->
                    routeDao.updateDownloadStatus(route.routeId, isDownloaded = false, downloadedAt = null)
                }
                
                Log.d("RouteRepositoryImpl", "✅ Cleared ${orphanedRoutes.size} orphaned downloads")
            } else {
                Log.d("RouteRepositoryImpl", "No orphaned downloads found")
            }
            
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RouteRepositoryImpl", "❌ Exception cleaning up orphaned downloads: ${e.message}", e)
            ApiResult.Error("Failed to cleanup orphaned downloads: ${e.message}")
        }
    }
}