package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.place.*
import retrofit2.Response
import retrofit2.http.*

interface PlaceApiService {
    
    @GET("places/city")
    suspend fun getPlacesByCity(
        @Header("Authorization") authorization: String,
        @Query("city") city: String
    ): Response<PlacesResponse>
    
    @POST("places/id")
    suspend fun getPlacesById(
        @Header("Authorization") authorization: String,
        @Body request: PlacesByIdsRequest
    ): Response<PlacesResponse>
    
    @POST("places/search")
    suspend fun searchPlaces(
        @Header("Authorization") authorization: String,
        @Body request: SearchPlacesRequest
    ): Response<PlacesResponse>
    
    @POST("places/autocomplete")
    suspend fun autocompletePlaces(
        @Header("Authorization") authorization: String,
        @Body request: AutocompletePlacesRequest
    ): Response<AutocompletePlacesResponse>

    @GET("places/top-rated")
    suspend fun getTopRatedPlaces(
        @Header("Authorization") authorization: String
    ): Response<TopRatedPlacesResponse>
}