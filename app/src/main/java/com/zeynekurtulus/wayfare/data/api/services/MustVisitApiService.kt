package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.place.MustVisitSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service for must-visit places search functionality
 */
interface MustVisitApiService {
    
    /**
     * Search for must-visit places in a specific city
     * 
     * @param city Name of the city to search places in (required)
     * @param query Search term for place names (optional, for autocomplete)
     * @param category Filter by wayfare_category (optional)
     * @param limit Maximum number of results (optional, default: 20, max: 50)
     * @return Response containing list of matching places
     */
    @GET("places/search-must-visit")
    suspend fun searchMustVisitPlaces(
        @Query("city") city: String,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<MustVisitSearchResponse>
}