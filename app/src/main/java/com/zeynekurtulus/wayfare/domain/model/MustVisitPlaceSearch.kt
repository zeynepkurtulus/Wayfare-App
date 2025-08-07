package com.zeynekurtulus.wayfare.domain.model

/**
 * Domain models for must-visit places search functionality
 */

data class MustVisitPlaceSearch(
    val placeId: String,
    val name: String,
    val category: String,
    val wayfareCategory: String,
    val rating: Double,
    val image: String?,
    val coordinates: PlaceCoordinates,
    val address: String,
    var isSelected: Boolean = false
)

data class PlaceCoordinates(
    val lat: Double,
    val lng: Double
)

/**
 * Categories available for filtering must-visit places
 */
enum class PlaceCategory(val displayName: String, val apiValue: String) {
    ALL("All Categories", ""),
    CULTURAL_SITES("Cultural Sites", "Cultural Sites"),
    ENTERTAINMENT("Entertainment", "Entertainment"),
    NATURE("Nature & Outdoor", "Nature"),
    FOOD_DRINK("Food & Drink", "Food & Drink"),
    SHOPPING("Shopping", "Shopping"),
    RELIGIOUS("Religious Sites", "Religious Sites"),
    MUSEUMS("Museums", "Museums"),
    HISTORICAL("Historical Sites", "Historical Sites")
}