package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.utils.ApiResult

interface CityRepository {
    suspend fun searchCities(query: String, limit: Int = 10): ApiResult<List<City>>
}