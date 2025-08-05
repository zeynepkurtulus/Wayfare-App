package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.route.*
import com.zeynekurtulus.wayfare.data.api.dto.user.ApiSuccessResponse
import retrofit2.Response
import retrofit2.http.*

interface RouteApiService {
    
    @POST("route/create")
    suspend fun createRoute(
        @Header("Authorization") authorization: String,
        @Body request: CreateRouteRequest
    ): Response<CreateRouteResponse>
    
    @GET("routes/user")
    suspend fun getUserRoutes(
        @Header("Authorization") authorization: String
    ): Response<UserRoutesResponse>
    
    @GET("routes/{route_id}")
    suspend fun getRoute(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String
    ): Response<SingleRouteResponse>
    
    @PUT("routes/{route_id}")
    suspend fun updateRoute(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String,
        @Body request: UpdateRouteRequest
    ): Response<ApiSuccessResponse>
    
    @DELETE("routes/{route_id}")
    suspend fun deleteRoute(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String
    ): Response<ApiSuccessResponse>
    
    @GET("routes/public")
    suspend fun getPublicRoutes(
        @Header("Authorization") authorization: String,
        @Query("category") category: String? = null,
        @Query("season") season: String? = null,
        @Query("budget") budget: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<PublicRoutesResponse>
}