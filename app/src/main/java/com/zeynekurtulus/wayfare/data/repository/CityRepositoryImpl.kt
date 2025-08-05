package com.zeynekurtulus.wayfare.data.repository

import com.zeynekurtulus.wayfare.data.api.services.CityApiService
import com.zeynekurtulus.wayfare.data.mappers.CityMapper
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.domain.repository.CityRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import com.zeynekurtulus.wayfare.utils.NetworkUtils

class CityRepositoryImpl(
    private val cityApiService: CityApiService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : CityRepository {
    
    override suspend fun searchCities(query: String, limit: Int): ApiResult<List<City>> {
        return try {
            android.util.Log.d("CityRepository", "Searching for cities with query: '$query', limit: $limit")
            
            // AuthInterceptor will automatically add the authorization header
            val response = cityApiService.searchCities(query, limit)
            
            android.util.Log.d("CityRepository", "API Response - Success: ${response.isSuccessful}, Code: ${response.code()}")
            
            if (response.isSuccessful) {
                val cityResponse = response.body()
                android.util.Log.d("CityRepository", "Response body - Success: ${cityResponse?.success}, Data size: ${cityResponse?.data?.size}")
                
                if (cityResponse?.success == true && cityResponse.data.isNotEmpty()) {
                    val cities = CityMapper.mapToCityList(cityResponse.data)
                    android.util.Log.d("CityRepository", "Mapped ${cities.size} cities successfully")
                    ApiResult.Success(cities)
                } else {
                    android.util.Log.w("CityRepository", "Empty response or unsuccessful: success=${cityResponse?.success}, data=${cityResponse?.data}")
                    ApiResult.Success(emptyList())
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CityRepository", "API call failed: ${response.code()}, Error body: $errorBody")
                ApiResult.Error("Failed to search cities: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("CityRepository", "Exception during city search: ${e.message}", e)
            NetworkUtils.handleApiError(e)
        }
    }
}