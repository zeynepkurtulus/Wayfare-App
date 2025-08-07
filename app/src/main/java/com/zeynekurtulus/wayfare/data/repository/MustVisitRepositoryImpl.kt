package com.zeynekurtulus.wayfare.data.repository

import android.util.Log
import com.zeynekurtulus.wayfare.data.api.services.MustVisitApiService
import com.zeynekurtulus.wayfare.data.mappers.MustVisitPlaceMapper
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch
import com.zeynekurtulus.wayfare.domain.repository.MustVisitRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils

/**
 * Implementation of MustVisitRepository
 */
class MustVisitRepositoryImpl(
    private val mustVisitApiService: MustVisitApiService
) : MustVisitRepository {
    
    override suspend fun searchMustVisitPlaces(
        city: String,
        query: String?,
        category: String?,
        limit: Int
    ): ApiResult<List<MustVisitPlaceSearch>> {
        return try {
            Log.d("MustVisitRepository", "Searching places - City: $city, Query: $query, Category: $category, Limit: $limit")
            
            val response = mustVisitApiService.searchMustVisitPlaces(
                city = city,
                query = query,
                category = category,
                limit = limit
            )
            
            Log.d("MustVisitRepository", "API Response - Success: ${response.isSuccessful}, Code: ${response.code()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.success) {
                    val places = MustVisitPlaceMapper.mapToMustVisitPlaceSearchList(responseBody.data)
                    Log.d("MustVisitRepository", "Found ${places.size} places for city: $city")
                    ApiResult.Success(places)
                } else {
                    val errorMessage = responseBody?.message ?: "Unknown error occurred"
                    Log.e("MustVisitRepository", "API returned error: $errorMessage")
                    ApiResult.Error(errorMessage)
                }
            } else {
                val errorMessage = "Failed to search places: HTTP ${response.code()}"
                Log.e("MustVisitRepository", "API call failed: $errorMessage")
                ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("MustVisitRepository", "Exception during places search: ${e.message}", e)
            ApiResult.Error("Failed to search places: ${e.message}")
        }
    }
}