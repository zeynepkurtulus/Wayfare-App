package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch
import com.zeynekurtulus.wayfare.utils.ApiResult

/**
 * Repository interface for must-visit places functionality
 */
interface MustVisitRepository {
    
    /**
     * Search for must-visit places in a specific city
     * 
     * @param city Name of the city to search places in
     * @param query Search term for place names (optional)
     * @param category Filter by wayfare_category (optional)
     * @param limit Maximum number of results
     * @return ApiResult containing list of matching places
     */
    suspend fun searchMustVisitPlaces(
        city: String,
        query: String? = null,
        category: String? = null,
        limit: Int = 20
    ): ApiResult<List<MustVisitPlaceSearch>>
}