package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.feedback.*
import retrofit2.Response
import retrofit2.http.*

interface FeedbackApiService {
    
    // Place Feedback endpoints
    @POST("feedback/place")
    suspend fun submitPlaceFeedback(
        @Header("Authorization") authorization: String,
        @Body request: PlaceFeedbackRequest
    ): Response<FeedbackResponse>
    
    @GET("feedback/place/{place_id}")
    suspend fun getPlaceFeedback(
        @Header("Authorization") authorization: String,
        @Path("place_id") placeId: String
    ): Response<PlaceFeedbackListResponse>
    
    @GET("feedback/place/{place_id}/user/{user_id}")
    suspend fun getUserPlaceFeedback(
        @Header("Authorization") authorization: String,
        @Path("place_id") placeId: String,
        @Path("user_id") userId: String
    ): Response<PlaceFeedbackListResponse>
    
    @PUT("feedback/place/{feedback_id}")
    suspend fun updatePlaceFeedback(
        @Header("Authorization") authorization: String,
        @Path("feedback_id") feedbackId: String,
        @Body request: UpdatePlaceFeedbackRequest
    ): Response<UpdateFeedbackResponse>
    
    @DELETE("feedback/place/{feedback_id}")
    suspend fun deletePlaceFeedback(
        @Header("Authorization") authorization: String,
        @Path("feedback_id") feedbackId: String
    ): Response<DeleteFeedbackResponse>
    
    @GET("feedback/place/{place_id}/stats")
    suspend fun getPlaceFeedbackStats(
        @Header("Authorization") authorization: String,
        @Path("place_id") placeId: String
    ): Response<FeedbackStatsResponse>
    
    // Route Feedback endpoints
    @POST("feedback/route")
    suspend fun submitRouteFeedback(
        @Header("Authorization") authorization: String,
        @Body request: RouteFeedbackRequest
    ): Response<FeedbackResponse>
    
    @GET("feedback/route/{route_id}")
    suspend fun getRouteFeedback(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String
    ): Response<RouteFeedbackListResponse>
    
    @GET("feedback/route/{route_id}/user/{user_id}")
    suspend fun getUserRouteFeedback(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String,
        @Path("user_id") userId: String
    ): Response<RouteFeedbackListResponse>
    
    @PUT("feedback/route/{feedback_id}")
    suspend fun updateRouteFeedback(
        @Header("Authorization") authorization: String,
        @Path("feedback_id") feedbackId: String,
        @Body request: UpdateRouteFeedbackRequest
    ): Response<UpdateFeedbackResponse>
    
    @DELETE("feedback/route/{feedback_id}")
    suspend fun deleteRouteFeedback(
        @Header("Authorization") authorization: String,
        @Path("feedback_id") feedbackId: String
    ): Response<DeleteFeedbackResponse>
    
    @GET("feedback/route/{route_id}/stats")
    suspend fun getRouteFeedbackStats(
        @Header("Authorization") authorization: String,
        @Path("route_id") routeId: String
    ): Response<FeedbackStatsResponse>
}


 
 
 
 