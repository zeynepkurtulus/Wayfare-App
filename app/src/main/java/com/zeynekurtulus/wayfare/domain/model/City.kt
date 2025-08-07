package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class City(
    val cityId: String,
    val name: String,
    val country: String,
    val countryId: String,
    val displayText: String,
    val coordinates: CityCoordinates
) : Parcelable

@Parcelize
data class CityCoordinates(
    val lat: Double,
    val lng: Double
) : Parcelable

// Trip Creation Models
@Parcelize
data class TripCreationData(
    var selectedCity: City? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var category: String? = null,
    var season: String? = null,
    var interests: List<String> = emptyList(),
    var budget: String? = null,
    var travelStyle: String? = null,
    var title: String? = null
) : Parcelable

// Category and Season Options
data class CategoryOption(
    val value: String,
    val label: String
)

data class SeasonOption(
    val value: String,
    val label: String
)

object TripConstants {
    val CATEGORIES = listOf(
        CategoryOption("city_break", "City Break"),
        CategoryOption("beach", "Beach"),
        CategoryOption("mountain", "Mountain"),
        CategoryOption("road_trip", "Road Trip")
    )
    
    val SEASONS = listOf(
        SeasonOption("spring", "Spring (Mar-May)"),
        SeasonOption("summer", "Summer (Jun-Aug)"),
        SeasonOption("autumn", "Autumn (Sep-Nov)"),
        SeasonOption("winter", "Winter (Dec-Feb)")
    )
    
    val BUDGETS = listOf(
        CategoryOption("budget", "Budget (Under $50/day)"),
        CategoryOption("mid_range", "Mid-range ($50-150/day)"),
        CategoryOption("luxury", "Luxury ($150+/day)")
    )
    
    val TRAVEL_STYLES = listOf(
        CategoryOption("solo", "Solo Traveler"),
        CategoryOption("couple", "Couple"),
        CategoryOption("family", "Family"),
        CategoryOption("group", "Group/Friends"),
        CategoryOption("business", "Business")
    )
}