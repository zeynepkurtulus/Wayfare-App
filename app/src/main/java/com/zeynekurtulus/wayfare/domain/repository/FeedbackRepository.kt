package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult

interface FeedbackRepository {
    
    // Place Feedback
    suspend fun submitPlaceFeedback(createPlaceFeedback: CreatePlaceFeedback): ApiResult<String>
    
    suspend fun getPlaceFeedback(placeId: String): ApiResult<List<PlaceFeedback>>
    
    suspend fun getUserPlaceFeedback(placeId: String, userId: String): ApiResult<List<PlaceFeedback>>
    
    suspend fun updatePlaceFeedback(feedbackId: String, updatePlaceFeedback: UpdatePlaceFeedback): ApiResult<Unit>
    
    suspend fun deletePlaceFeedback(feedbackId: String): ApiResult<Unit>
    
    suspend fun getPlaceFeedbackStats(placeId: String): ApiResult<FeedbackStats>
    
    // Route Feedback
    suspend fun submitRouteFeedback(createRouteFeedback: CreateRouteFeedback): ApiResult<String>
    
    suspend fun getRouteFeedback(routeId: String): ApiResult<List<RouteFeedback>>
    
    suspend fun getUserRouteFeedback(routeId: String, userId: String): ApiResult<List<RouteFeedback>>
    
    suspend fun updateRouteFeedback(feedbackId: String, updateRouteFeedback: UpdateRouteFeedback): ApiResult<Unit>
    
    suspend fun deleteRouteFeedback(feedbackId: String): ApiResult<Unit>
    
    suspend fun getRouteFeedbackStats(routeId: String): ApiResult<FeedbackStats>
}