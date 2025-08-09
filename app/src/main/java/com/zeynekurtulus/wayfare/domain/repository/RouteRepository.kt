package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult

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
}