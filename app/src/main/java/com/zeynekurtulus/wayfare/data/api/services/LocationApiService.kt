package com.zeynekurtulus.wayfare.data.api.services

import com.zeynekurtulus.wayfare.data.api.dto.location.*
import retrofit2.Response
import retrofit2.http.*

interface LocationApiService {
    
    // Cities endpoints
    @GET("cities/all")
    suspend fun getAllCities(
        @Header("Authorization") authorization: String
    ): Response<CitiesResponse>
    
    @POST("cities/specific")
    suspend fun getCitiesByCountry(
        @Header("Authorization") authorization: String,
        @Body request: CitiesByCountryRequest
    ): Response<CitiesResponse>
    
    // Countries endpoints
    @GET("countries/all")
    suspend fun getAllCountries(
        @Header("Authorization") authorization: String
    ): Response<CountriesResponse>
    
    @POST("countries/region")
    suspend fun getCountriesByRegion(
        @Header("Authorization") authorization: String,
        @Body request: CountriesByRegionRequest
    ): Response<CountriesResponse>
    
    @POST("countries/search")
    suspend fun searchCountries(
        @Header("Authorization") authorization: String,
        @Body request: SearchCountriesRequest
    ): Response<CountriesResponse>
    
    @GET("countries/allRegions")
    suspend fun getAllRegions(
        @Header("Authorization") authorization: String
    ): Response<RegionsResponse>
}