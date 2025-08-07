package com.zeynekurtulus.wayfare.data.api.dto.route

import com.google.gson.annotations.SerializedName

// Request DTOs
data class CreateRouteRequest(
    @SerializedName("title") val title: String,
    @SerializedName("city") val city: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("category") val category: String,
    @SerializedName("season") val season: String,
    @SerializedName("must_visit") val mustVisit: List<MustVisitPlaceDto>
)

data class UpdateRouteRequest(
    @SerializedName("title") val title: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("season") val season: String?,
    @SerializedName("must_visit") val mustVisit: List<MustVisitPlaceDto>?
)

data class MustVisitPlaceDto(
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("address") val address: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("source") val source: String,
    @SerializedName("coordinates") val coordinates: CoordinatesDto?
)

data class RouteDayDto(
    @SerializedName("date") val date: String,
    @SerializedName("activities") val activities: List<ActivityDto>
)

data class ActivityDto(
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("time") val time: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("image") val image: String?
)

data class CoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

// Response DTOs
data class CreateRouteResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("route_id") val routeId: String,
    @SerializedName("status_code") val statusCode: Int
)

data class UserRoutesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<RouteDto>
)

data class SingleRouteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: RouteDetailDto
)

data class PublicRoutesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("data") val data: List<RouteDto>
)

data class RouteDto(
    @SerializedName("route_id") val routeId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("city") val city: String,
    @SerializedName("city_id") val cityId: String,
    @SerializedName("country") val country: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("budget") val budget: String,
    @SerializedName("travel_style") val travelStyle: String,
    @SerializedName("category") val category: String,
    @SerializedName("season") val season: String,
    @SerializedName("stats") val stats: RouteStatsDto,
    @SerializedName("must_visit") val mustVisit: List<MustVisitPlaceDetailDto>,
    @SerializedName("days") val days: List<RouteDayDto>,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class RouteDetailDto(
    @SerializedName("route_id") val routeId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("city") val city: String,
    @SerializedName("city_id") val cityId: String?,
    @SerializedName("country") val country: String,
    @SerializedName("country_id") val countryId: String?,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("budget") val budget: String,
    @SerializedName("travel_style") val travelStyle: String,
    @SerializedName("category") val category: String,
    @SerializedName("season") val season: String,
    @SerializedName("stats") val stats: RouteStatsDto,
    @SerializedName("image") val image: String?,
    @SerializedName("must_visit") val mustVisit: List<MustVisitPlaceDetailDto>,
    @SerializedName("days") val days: List<RouteDayDto>,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class MustVisitPlaceDetailDto(
    @SerializedName("place_id") val placeId: String?,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("address") val address: String?,
    @SerializedName("coordinates") val coordinates: CoordinatesDto?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("source") val source: String,
    @SerializedName("opening_hours") val openingHours: Map<String, String>?
)

data class RouteStatsDto(
    @SerializedName("views_count") val viewsCount: Int,
    @SerializedName("copies_count") val copiesCount: Int,
    @SerializedName("likes_count") val likesCount: Int
)

