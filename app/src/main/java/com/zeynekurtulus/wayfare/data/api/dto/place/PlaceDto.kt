package com.zeynekurtulus.wayfare.data.api.dto.place

import com.google.gson.annotations.SerializedName
import com.zeynekurtulus.wayfare.data.api.dto.route.CoordinatesDto

// Request DTOs
data class PlacesByIdsRequest(
    @SerializedName("place_ids") val placeIds: List<String>
)

data class SearchPlacesRequest(
    @SerializedName("city") val city: String,                    // REQUIRED - supports partial search
    @SerializedName("category") val category: String?,           // OPTIONAL - searches both 'category' and 'wayfare_category' fields
    @SerializedName("budget") val budget: String?,               // OPTIONAL - values: "low", "medium", "high"
    @SerializedName("rating") val rating: Double?,               // OPTIONAL - exact rating match (only if > 0)
    @SerializedName("name") val name: String?,                   // OPTIONAL - partial name search
    @SerializedName("country") val country: String?,             // OPTIONAL - partial country search
    @SerializedName("min_rating") val minRating: Double?,        // OPTIONAL - minimum rating filter (only if > 0)
    @SerializedName("keywords") val keywords: String?,           // OPTIONAL - text search across place descriptions
    @SerializedName("limit") val limit: Int = 20                 // OPTIONAL - max results (default: 10)
)

data class AutocompletePlacesRequest(
    @SerializedName("query") val query: String,
    @SerializedName("city") val city: String,
    @SerializedName("limit") val limit: Int = 5
)



// Response DTOs
data class PlacesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<PlaceDto>
)

data class AutocompletePlacesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<AutocompletePlaceDto>
)

data class PlaceDto(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String?,
    @SerializedName("coordinates") val coordinates: CoordinatesDto?,
    @SerializedName("category") val category: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("price_level") val priceLevel: Int?,
    @SerializedName("opening_hours") val openingHours: Map<String, String>?,
    @SerializedName("image") val image: String?,
    @SerializedName("detail_url") val detailUrl: String?,
    @SerializedName("duration") val duration: Int? // in minutes
)

data class AutocompletePlaceDto(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String?
)
data class TopRatedDto(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("category") val category: String?,
    @SerializedName("wayfare_category") val wayfareCategory: String,
    @SerializedName("price") val price : String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("wayfare_rating") val wayfareRating: Double?,
    @SerializedName("total_feedback_count") val totalFeedbackCount: Int,
    @SerializedName("image") val image: String?,
    @SerializedName("detail_url") val detailUrl: String?,
    @SerializedName("opening_hours") val openingHours: Map<String, String>?,
    @SerializedName("coordinates") val coordinates: CoordinatesDto?,
    @SerializedName("address") val address: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("country_id") val countryId: String?,
    @SerializedName("city_id") val cityId: String?,
    @SerializedName("popularity") val popularity: Double?, // New field for popularity score
    @SerializedName("duration") val duration: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
data class TopRatedPlacesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<TopRatedDto>
)
 
 
 