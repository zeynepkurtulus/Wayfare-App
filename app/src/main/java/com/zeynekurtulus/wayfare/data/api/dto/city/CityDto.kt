package com.zeynekurtulus.wayfare.data.api.dto.city

import com.google.gson.annotations.SerializedName

// City Search Response DTOs
data class CitySearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<CityDto>
)

data class CityDto(
    @SerializedName("city_id") val cityId: String,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("display_text") val displayText: String,
    @SerializedName("coordinates") val coordinates: CoordinatesDto
)

data class CoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)