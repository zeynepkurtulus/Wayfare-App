package com.zeynekurtulus.wayfare.data.repository

import com.zeynekurtulus.wayfare.data.api.services.FeedbackApiService
import com.zeynekurtulus.wayfare.data.mappers.FeedbackMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.FeedbackRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

class FeedbackRepositoryImpl(
    private val feedbackApiService: FeedbackApiService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : FeedbackRepository {
    
    // Place Feedback Methods
    override suspend fun submitPlaceFeedback(createPlaceFeedback: CreatePlaceFeedback): ApiResult<String> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = FeedbackMapper.mapToPlaceFeedbackRequest(createPlaceFeedback)
            val response = feedbackApiService.submitPlaceFeedback("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { feedbackResponse ->
                    feedbackResponse.feedbackId?.let { 
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Failed to submit place feedback")
                } ?: ApiResult.Error("Failed to submit place feedback")
            } else {
                ApiResult.Error("Failed to submit place feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getPlaceFeedback(placeId: String): ApiResult<List<PlaceFeedback>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getPlaceFeedback("Bearer $token", placeId)
            
            if (response.isSuccessful) {
                response.body()?.let { placeFeedbackListResponse ->
                    val feedback = placeFeedbackListResponse.data.map { FeedbackMapper.mapToPlaceFeedback(it) }
                    ApiResult.Success(feedback)
                } ?: ApiResult.Error("Failed to get place feedback")
            } else {
                ApiResult.Error("Failed to get place feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getUserPlaceFeedback(placeId: String, userId: String): ApiResult<List<PlaceFeedback>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getUserPlaceFeedback("Bearer $token", placeId, userId)
            
            if (response.isSuccessful) {
                response.body()?.let { placeFeedbackListResponse ->
                    val feedback = placeFeedbackListResponse.data.map { FeedbackMapper.mapToPlaceFeedback(it) }
                    ApiResult.Success(feedback)
                } ?: ApiResult.Error("Failed to get user place feedback")
            } else {
                ApiResult.Error("Failed to get user place feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun updatePlaceFeedback(
        feedbackId: String, 
        updatePlaceFeedback: UpdatePlaceFeedback
    ): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = FeedbackMapper.mapToUpdatePlaceFeedbackRequest(updatePlaceFeedback)
            val response = feedbackApiService.updatePlaceFeedback("Bearer $token", feedbackId, request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to update place feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun deletePlaceFeedback(feedbackId: String): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.deletePlaceFeedback("Bearer $token", feedbackId)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete place feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getPlaceFeedbackStats(placeId: String): ApiResult<FeedbackStats> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getPlaceFeedbackStats("Bearer $token", placeId)
            
            if (response.isSuccessful) {
                response.body()?.let { feedbackStatsResponse ->
                    val stats = FeedbackMapper.mapToFeedbackStats(feedbackStatsResponse.data)
                    ApiResult.Success(stats)
                } ?: ApiResult.Error("Failed to get place feedback stats")
            } else {
                ApiResult.Error("Failed to get place feedback stats", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    // Route Feedback Methods
    override suspend fun submitRouteFeedback(createRouteFeedback: CreateRouteFeedback): ApiResult<String> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = FeedbackMapper.mapToRouteFeedbackRequest(createRouteFeedback)
            val response = feedbackApiService.submitRouteFeedback("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { feedbackResponse ->
                    feedbackResponse.feedbackId?.let { 
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Failed to submit route feedback")
                } ?: ApiResult.Error("Failed to submit route feedback")
            } else {
                ApiResult.Error("Failed to submit route feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getRouteFeedback(routeId: String): ApiResult<List<RouteFeedback>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getRouteFeedback("Bearer $token", routeId)
            
            if (response.isSuccessful) {
                response.body()?.let { routeFeedbackListResponse ->
                    val feedback = routeFeedbackListResponse.data.map { FeedbackMapper.mapToRouteFeedback(it) }
                    ApiResult.Success(feedback)
                } ?: ApiResult.Error("Failed to get route feedback")
            } else {
                ApiResult.Error("Failed to get route feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getUserRouteFeedback(routeId: String, userId: String): ApiResult<List<RouteFeedback>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getUserRouteFeedback("Bearer $token", routeId, userId)
            
            if (response.isSuccessful) {
                response.body()?.let { routeFeedbackListResponse ->
                    val feedback = routeFeedbackListResponse.data.map { FeedbackMapper.mapToRouteFeedback(it) }
                    ApiResult.Success(feedback)
                } ?: ApiResult.Error("Failed to get user route feedback")
            } else {
                ApiResult.Error("Failed to get user route feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun updateRouteFeedback(
        feedbackId: String, 
        updateRouteFeedback: UpdateRouteFeedback
    ): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = FeedbackMapper.mapToUpdateRouteFeedbackRequest(updateRouteFeedback)
            val response = feedbackApiService.updateRouteFeedback("Bearer $token", feedbackId, request)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to update route feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun deleteRouteFeedback(feedbackId: String): ApiResult<Unit> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.deleteRouteFeedback("Bearer $token", feedbackId)
            
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Failed to delete route feedback", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getRouteFeedbackStats(routeId: String): ApiResult<FeedbackStats> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = feedbackApiService.getRouteFeedbackStats("Bearer $token", routeId)
            
            if (response.isSuccessful) {
                response.body()?.let { feedbackStatsResponse ->
                    val stats = FeedbackMapper.mapToFeedbackStats(feedbackStatsResponse.data)
                    ApiResult.Success(stats)
                } ?: ApiResult.Error("Failed to get route feedback stats")
            } else {
                ApiResult.Error("Failed to get route feedback stats", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
}