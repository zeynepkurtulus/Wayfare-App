package com.zeynekurtulus.wayfare.data.api.dto.location

import com.google.gson.annotations.SerializedName
import com.zeynekurtulus.wayfare.data.api.dto.route.CoordinatesDto

// Request DTOs
data class CitiesByCountryRequest(
    @SerializedName("country") val country: String
)

data class CountriesByRegionRequest(
    @SerializedName("region") val region: String
)

data class SearchCountriesRequest(
    @SerializedName("query") val query: String,
    @SerializedName("limit") val limit: Int = 10
)

// Response DTOs
data class CitiesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<CityDto>
)

data class CountriesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<CountryDto>
)

data class RegionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<String>
)

data class CityDto(
    @SerializedName("city_id") val cityId: String,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("coordinates") val coordinates: CoordinatesDto?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CountryDto(
    @SerializedName("name") val name: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("region") val region: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class SimpleCountryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String
)


 
 
 