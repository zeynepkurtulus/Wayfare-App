package com.zeynekurtulus.wayfare.domain.repository

import com.zeynekurtulus.wayfare.domain.model.*
import com.zeynekurtulus.wayfare.utils.ApiResult

interface PlaceRepository {
    
    suspend fun getPlacesByCity(city: String): ApiResult<List<Place>>
    
    suspend fun getPlacesById(placeIds: List<String>): ApiResult<List<Place>>
    
    suspend fun searchPlaces(searchPlaces: SearchPlaces): ApiResult<List<Place>>
    
    suspend fun autocompletePlaces(autocompletePlaces: AutocompletePlaces): ApiResult<List<AutocompletePlace>>
    
    suspend fun getTopRatedPlaces(): ApiResult<List<TopRatedPlace>>
}