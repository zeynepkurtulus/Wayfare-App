package com.zeynekurtulus.wayfare.data.repository

import android.util.Log
import com.zeynekurtulus.wayfare.data.api.services.RouteApiService
import com.zeynekurtulus.wayfare.data.mappers.RouteMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.RouteRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

class RouteRepositoryImpl(
    private val routeApiService: RouteApiService,
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
    
    // ⭐ NEW: Privacy toggle implementation
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
    
    // ⭐ NEW: Advanced public route search implementation
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
}