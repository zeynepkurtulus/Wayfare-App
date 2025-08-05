package com.zeynekurtulus.wayfare.data.repository

import com.zeynekurtulus.wayfare.data.api.services.LocationApiService
import com.zeynekurtulus.wayfare.data.mappers.LocationMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.LocationRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

class LocationRepositoryImpl(
    private val locationApiService: LocationApiService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : LocationRepository {
    
    override suspend fun getAllCities(): ApiResult<List<LocationCity>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = locationApiService.getAllCities("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { citiesResponse ->
                    val cities = citiesResponse.data.map { LocationMapper.mapToLocationCity(it) }
                    ApiResult.Success(cities)
                } ?: ApiResult.Error("Failed to get cities")
            } else {
                ApiResult.Error("Failed to get cities", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getCitiesByCountry(country: String): ApiResult<List<LocationCity>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = LocationMapper.mapToCitiesByCountryRequest(country)
            val response = locationApiService.getCitiesByCountry("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { citiesResponse ->
                    val cities = citiesResponse.data.map { LocationMapper.mapToLocationCity(it) }
                    ApiResult.Success(cities)
                } ?: ApiResult.Error("Failed to get cities")
            } else {
                ApiResult.Error("Failed to get cities for country", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getAllCountries(): ApiResult<List<Country>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = locationApiService.getAllCountries("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { countriesResponse ->
                    val countries = countriesResponse.data.map { LocationMapper.mapToCountry(it) }
                    ApiResult.Success(countries)
                } ?: ApiResult.Error("Failed to get countries")
            } else {
                ApiResult.Error("Failed to get countries", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getCountriesByRegion(region: String): ApiResult<List<Country>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = LocationMapper.mapToCountriesByRegionRequest(region)
            val response = locationApiService.getCountriesByRegion("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { countriesResponse ->
                    // Note: This endpoint returns SimpleCountryDto based on API docs
                    val countries = if (countriesResponse.data.isNotEmpty()) {
                        // If it's the regular CountryDto format
                        try {
                            countriesResponse.data.map { LocationMapper.mapToCountry(it) }
                        } catch (e: Exception) {
                            // If it's the SimpleCountryDto format, we'll need to handle it differently
                            // For now, return empty list and log the issue
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    ApiResult.Success(countries)
                } ?: ApiResult.Error("Failed to get countries by region")
            } else {
                ApiResult.Error("Failed to get countries by region", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun searchCountries(searchCountries: SearchCountries): ApiResult<List<Country>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = LocationMapper.mapToSearchCountriesRequest(searchCountries)
            val response = locationApiService.searchCountries("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { countriesResponse ->
                    // Note: This endpoint returns SimpleCountryDto based on API docs
                    val countries = if (countriesResponse.data.isNotEmpty()) {
                        // If it's the regular CountryDto format
                        try {
                            countriesResponse.data.map { LocationMapper.mapToCountry(it) }
                        } catch (e: Exception) {
                            // If it's the SimpleCountryDto format, we'll need to handle it differently
                            // For now, return empty list and log the issue
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    ApiResult.Success(countries)
                } ?: ApiResult.Error("Failed to search countries")
            } else {
                ApiResult.Error("Failed to search countries", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getAllRegions(): ApiResult<List<String>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = locationApiService.getAllRegions("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { regionsResponse ->
                    ApiResult.Success(regionsResponse.data)
                } ?: ApiResult.Error("Failed to get regions")
            } else {
                ApiResult.Error("Failed to get regions", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
}