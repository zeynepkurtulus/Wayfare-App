package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Route(
    val routeId: String,
    val userId: String,
    val title: String,
    val city: String,
    val cityId: String?,
    val country: String,
    val countryId: String?,
    val startDate: String,
    val endDate: String,
    val budget: String,
    val travelStyle: String,
    val category: String,
    val season: String,
    val stats: RouteStats,
    val mustVisit: List<MustVisitPlace>,
    val days: List<RouteDay>,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable

@Parcelize
data class RouteDetail(
    val routeId: String,
    val userId: String,
    val title: String,
    val city: String,
    val cityId: String?,
    val country: String,
    val countryId: String?,
    val startDate: String,
    val endDate: String,
    val budget: String,
    val travelStyle: String,
    val category: String,
    val season: String,
    val stats: RouteStats,
    val mustVisit: List<MustVisitPlace>,
    val days: List<RouteDay>,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable

@Parcelize
data class RouteStats(
    val viewsCount: Int,
    val copiesCount: Int,
    val likesCount: Int
) : Parcelable

@Parcelize
data class MustVisitPlace(
    val placeId: String?,
    val placeName: String,
    val address: String?,
    val coordinates: Coordinates?,
    val notes: String?,
    val source: String,
    val openingHours: Map<String, String>?
) : Parcelable

@Parcelize
data class RouteDay(
    val date: String,
    val activities: List<Activity>
) : Parcelable

@Parcelize
data class Activity(
    val placeId: String?,
    val placeName: String,
    val time: String,
    val notes: String?,
    val image: String?
) : Parcelable

@Parcelize
data class Coordinates(
    val lat: Double,
    val lng: Double
) : Parcelable

data class CreateRoute(
    val title: String,
    val city: String,
    val startDate: String,
    val endDate: String,
    val category: String,
    val season: String
)

data class UpdateRoute(
    val title: String?,
    val city: String?,
    val startDate: String?,
    val endDate: String?,
    val category: String?,
    val season: String?,
    val mustVisit: List<MustVisitPlace>?
)