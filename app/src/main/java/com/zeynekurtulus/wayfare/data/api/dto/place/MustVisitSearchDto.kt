package com.zeynekurtulus.wayfare.data.api.dto.place

import com.google.gson.annotations.SerializedName

/**
 * DTOs for must-visit places search endpoint
 * Used for /places/search-must-visit API calls
 */

data class MustVisitSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<MustVisitPlaceSearchDto>
)

data class MustVisitPlaceSearchDto(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("wayfare_category") val wayfareCategory: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("image") val image: String?,
    @SerializedName("coordinates") val coordinates: PlaceCoordinatesDto?,
    @SerializedName("address") val address: String?
)

data class PlaceCoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)