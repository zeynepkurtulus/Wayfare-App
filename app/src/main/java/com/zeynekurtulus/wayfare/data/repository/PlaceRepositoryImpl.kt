package com.zeynekurtulus.wayfare.data.repository

import com.zeynekurtulus.wayfare.data.api.services.PlaceApiService
import com.zeynekurtulus.wayfare.data.mappers.PlaceMapper
import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.domain.repository.PlaceRepository
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.NetworkUtils
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager

class PlaceRepositoryImpl(
    private val placeApiService: PlaceApiService,
    private val sharedPreferencesManager: SharedPreferencesManager
) : PlaceRepository {
    
    override suspend fun getPlacesByCity(city: String): ApiResult<List<Place>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = placeApiService.getPlacesByCity("Bearer $token", city)
            
            if (response.isSuccessful) {
                response.body()?.let { placesResponse ->
                    val places = placesResponse.data.map { PlaceMapper.mapToPlace(it) }
                    ApiResult.Success(places)
                } ?: ApiResult.Error("Failed to get places")
            } else {
                ApiResult.Error("Failed to get places for city", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getPlacesById(placeIds: List<String>): ApiResult<List<Place>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = PlaceMapper.mapToPlacesByIdsRequest(placeIds)
            val response = placeApiService.getPlacesById("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { placesResponse ->
                    val places = placesResponse.data.map { PlaceMapper.mapToPlace(it) }
                    ApiResult.Success(places)
                } ?: ApiResult.Error("Failed to get places")
            } else {
                ApiResult.Error("Failed to get places by IDs", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun searchPlaces(searchPlaces: SearchPlaces): ApiResult<List<Place>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            android.util.Log.d("PlaceRepositoryImpl", "Searching places with parameters:")
            android.util.Log.d("PlaceRepositoryImpl", "  name: '${searchPlaces.name}'")
            android.util.Log.d("PlaceRepositoryImpl", "  city: '${searchPlaces.city}'")
            android.util.Log.d("PlaceRepositoryImpl", "  category: '${searchPlaces.category}'")
            android.util.Log.d("PlaceRepositoryImpl", "  limit: ${searchPlaces.limit}")
            
            val request = PlaceMapper.mapToSearchPlacesRequest(searchPlaces)
            val response = placeApiService.searchPlaces("Bearer $token", request)
            
            android.util.Log.d("PlaceRepositoryImpl", "API response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body()?.let { placesResponse ->
                    android.util.Log.d("PlaceRepositoryImpl", "Response body success: ${placesResponse.success}, message: ${placesResponse.message}")
                    android.util.Log.d("PlaceRepositoryImpl", "Places count: ${placesResponse.data.size}")
                    
                    if (placesResponse.success) {
                        val places = placesResponse.data.map { PlaceMapper.mapToPlace(it) }
                        ApiResult.Success(places)
                    } else {
                        ApiResult.Error("API Error: ${placesResponse.message}")
                    }
                } ?: run {
                    android.util.Log.e("PlaceRepositoryImpl", "Response body is null")
                    ApiResult.Error("Failed to search places - null response body")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("PlaceRepositoryImpl", "API error - code: ${response.code()}, error body: $errorBody")
                ApiResult.Error("Failed to search places - HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            android.util.Log.e("PlaceRepositoryImpl", "Exception searching places", e)
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun autocompletePlaces(autocompletePlaces: AutocompletePlaces): ApiResult<List<AutocompletePlace>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val request = PlaceMapper.mapToAutocompletePlacesRequest(autocompletePlaces)
            val response = placeApiService.autocompletePlaces("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { autocompletePlacesResponse ->
                    val places = autocompletePlacesResponse.data.map { PlaceMapper.mapToAutocompletePlace(it) }
                    ApiResult.Success(places)
                } ?: ApiResult.Error("Failed to get autocomplete suggestions")
            } else {
                ApiResult.Error("Failed to get autocomplete suggestions", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
    
    override suspend fun getTopRatedPlaces(): ApiResult<List<TopRatedPlace>> {
        return try {
            val token = sharedPreferencesManager.getAccessToken()
                ?: return ApiResult.Error("User not authenticated")
            
            val response = placeApiService.getTopRatedPlaces("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { topRatedPlacesResponse ->
                    val topRatedPlaces = topRatedPlacesResponse.data.map { PlaceMapper.mapToTopRatedPlace(it) }
                    ApiResult.Success(topRatedPlaces)
                } ?: ApiResult.Error("Failed to get top rated places")
            } else {
                ApiResult.Error("Failed to get top rated places", response.code())
            }
        } catch (e: Exception) {
            NetworkUtils.handleApiError(e)
        }
    }
}