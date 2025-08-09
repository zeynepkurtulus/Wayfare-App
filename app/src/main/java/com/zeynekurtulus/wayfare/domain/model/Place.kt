package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Place(
    val placeId: String,
    val name: String,
    val address: String?,
    val coordinates: Coordinates?,
    val category: String?,
    val rating: Double?,
    val priceLevel: Int?,
    val openingHours: Map<String, String>?,
    val image: String?,
    val detailUrl: String?,
    val duration: Int? // in minutes
) : Parcelable

@Parcelize
data class AutocompletePlace(
    val placeId: String,
    val name: String,
    val category: String?
) : Parcelable

data class SearchPlaces(
    val city: String,                    // REQUIRED - supports partial search
    val category: String? = null,        // OPTIONAL - searches both 'category' and 'wayfare_category' fields
    val budget: String? = null,          // OPTIONAL - values: "low", "medium", "high"
    val rating: Double? = null,          // OPTIONAL - exact rating match (only if > 0)
    val name: String? = null,            // OPTIONAL - partial name search
    val country: String? = null,         // OPTIONAL - partial country search
    val minRating: Double? = null,       // OPTIONAL - minimum rating filter (only if > 0)
    val keywords: String? = null,        // OPTIONAL - text search across place descriptions
    val limit: Int = 10                  // OPTIONAL - max results (default: 10)
)

data class AutocompletePlaces(
    val query: String,
    val city: String,
    val limit: Int = 5
)

@Parcelize
data class TopRatedPlace(
    val placeId: String,
    val name: String,
    val city: String,
    val category: String?,
    val wayfareCategory: String,
    val price: String?,
    val rating: Double?,
    val wayfareRating: Double?,
    val totalFeedbackCount: Int,
    val image: String?,
    val detailUrl: String?,
    val openingHours: Map<String, String>?,
    val coordinates: Coordinates?,
    val address: String?,
    val source: String?,
    val country: String?,
    val countryId: String?,
    val cityId: String?,
    val popularity: Double?,
    val duration: Int?,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable