package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult

interface LocationRepository {
    
    // Cities
    suspend fun getAllCities(): ApiResult<List<LocationCity>>
    
    suspend fun getCitiesByCountry(country: String): ApiResult<List<LocationCity>>
    
    // Countries
    suspend fun getAllCountries(): ApiResult<List<Country>>
    
    suspend fun getCountriesByRegion(region: String): ApiResult<List<Country>>
    
    suspend fun searchCountries(searchCountries: SearchCountries): ApiResult<List<Country>>
    
    suspend fun getAllRegions(): ApiResult<List<String>>
}