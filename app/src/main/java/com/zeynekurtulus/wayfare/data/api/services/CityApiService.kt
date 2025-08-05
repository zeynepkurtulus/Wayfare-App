package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.city.CitySearchResponse
import retrofit2.Response
import retrofit2.http.*

interface CityApiService {
    
    @GET("cities/search")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Response<CitySearchResponse>
}