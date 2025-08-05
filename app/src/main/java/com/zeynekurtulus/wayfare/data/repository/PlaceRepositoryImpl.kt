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
            
            val request = PlaceMapper.mapToSearchPlacesRequest(searchPlaces)
            val response = placeApiService.searchPlaces("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { placesResponse ->
                    val places = placesResponse.data.map { PlaceMapper.mapToPlace(it) }
                    ApiResult.Success(places)
                } ?: ApiResult.Error("Failed to search places")
            } else {
                ApiResult.Error("Failed to search places", response.code())
            }
        } catch (e: Exception) {
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