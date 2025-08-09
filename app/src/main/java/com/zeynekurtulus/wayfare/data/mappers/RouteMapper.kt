package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.route.*
import com.zeynekurtulus.wayfare.domain.model.*

object RouteMapper {
    
    fun mapToCreateRouteRequest(createRoute: CreateRoute): CreateRouteRequest {
        return CreateRouteRequest(
            title = createRoute.title,
            city = createRoute.city,
            startDate = createRoute.startDate,
            endDate = createRoute.endDate,
            category = createRoute.category,
            season = createRoute.season,
            mustVisit = createRoute.mustVisit.map { mapToMustVisitPlaceDto(it) },
            isPublic = createRoute.isPublic  
        )
    }
    
    fun mapToUpdateRouteRequest(updateRoute: UpdateRoute): UpdateRouteRequest {
        return UpdateRouteRequest(
            title = updateRoute.title,
            city = updateRoute.city,
            startDate = updateRoute.startDate,
            endDate = updateRoute.endDate,
            category = updateRoute.category,
            season = updateRoute.season,
            mustVisit = updateRoute.mustVisit?.map { mapToMustVisitPlaceDto(it) }
        )
    }
    
    fun mapToRoute(routeDto: RouteDto): Route {
        return try {
            android.util.Log.d("RouteMapper", "🔄 Starting to map route: ${routeDto.title}")
            android.util.Log.d("RouteMapper", "📋 Field values:")
            android.util.Log.d("RouteMapper", "  routeId: ${routeDto.routeId}")
            android.util.Log.d("RouteMapper", "  userId: ${routeDto.userId}")
            android.util.Log.d("RouteMapper", "  title: ${routeDto.title}")
            android.util.Log.d("RouteMapper", "  city: ${routeDto.city}")
            android.util.Log.d("RouteMapper", "  cityId: ${routeDto.cityId} (nullable)")
            android.util.Log.d("RouteMapper", "  country: ${routeDto.country}")
            android.util.Log.d("RouteMapper", "  countryId: ${routeDto.countryId} (nullable)")
            android.util.Log.d("RouteMapper", "  startDate: ${routeDto.startDate}")
            android.util.Log.d("RouteMapper", "  endDate: ${routeDto.endDate}")
            android.util.Log.d("RouteMapper", "  budget: ${routeDto.budget}")
            android.util.Log.d("RouteMapper", "  travelStyle: ${routeDto.travelStyle}")
            android.util.Log.d("RouteMapper", "  category: ${routeDto.category}")
            android.util.Log.d("RouteMapper", "  season: ${routeDto.season}")
            android.util.Log.d("RouteMapper", "  createdAt: ${routeDto.createdAt} (nullable)")
            android.util.Log.d("RouteMapper", "  updatedAt: ${routeDto.updatedAt} (nullable)")
            
            Route(
                routeId = routeDto.routeId,
                userId = routeDto.userId,
                title = routeDto.title,
                city = routeDto.city,
                cityId = routeDto.cityId,
                country = routeDto.country,
                countryId = routeDto.countryId,
                startDate = routeDto.startDate,
                endDate = routeDto.endDate,
                budget = routeDto.budget,
                travelStyle = routeDto.travelStyle,
                category = routeDto.category,
                season = routeDto.season,
                stats = mapToRouteStats(routeDto.stats),
                mustVisit = routeDto.mustVisit.map { mapToMustVisitPlace(it) },
                days = routeDto.days.map { mapToRouteDay(it) },
                createdAt = routeDto.createdAt,
                updatedAt = routeDto.updatedAt,
                isPublic = routeDto.isPublic  
            )
        } catch (e: Exception) {
            android.util.Log.e("RouteMapper", "❌ ERROR mapping RouteDto to Route: ${e.message}", e)
            android.util.Log.e("RouteMapper", "❌ Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e("RouteMapper", "❌ Failed RouteDto: routeId=${routeDto.routeId}, title=${routeDto.title}")
            throw e
        }
    }
    
    fun mapToRouteDetail(routeDetailDto: RouteDetailDto): RouteDetail {
        return RouteDetail(
            routeId = routeDetailDto.routeId,
            userId = routeDetailDto.userId,
            title = routeDetailDto.title,
            city = routeDetailDto.city,
            cityId = routeDetailDto.cityId,
            country = routeDetailDto.country,
            countryId = routeDetailDto.countryId,
            startDate = routeDetailDto.startDate,
            endDate = routeDetailDto.endDate,
            budget = routeDetailDto.budget,
            travelStyle = routeDetailDto.travelStyle,
            category = routeDetailDto.category,
            season = routeDetailDto.season,
            stats = mapToRouteStats(routeDetailDto.stats),
            mustVisit = routeDetailDto.mustVisit.map { mapToMustVisitPlace(it) },
            days = routeDetailDto.days.map { mapToRouteDay(it) },
            createdAt = routeDetailDto.createdAt,
            updatedAt = routeDetailDto.updatedAt,
            isPublic = routeDetailDto.isPublic  
        )
    }
    
    private fun mapToMustVisitPlaceDto(mustVisitPlace: MustVisitPlace): MustVisitPlaceDto {
        return MustVisitPlaceDto(
            placeId = mustVisitPlace.placeId,
            placeName = mustVisitPlace.placeName,
            address = mustVisitPlace.address,
            notes = mustVisitPlace.notes,
            source = mustVisitPlace.source,
            coordinates = mustVisitPlace.coordinates?.let { mapToCoordinatesDto(it) },
            image = mustVisitPlace.image
        )
    }
    
    private fun mapToMustVisitPlace(mustVisitPlaceDto: MustVisitPlaceDto): MustVisitPlace {
        return MustVisitPlace(
            placeId = mustVisitPlaceDto.placeId,
            placeName = mustVisitPlaceDto.placeName,
            address = mustVisitPlaceDto.address,
            coordinates = mustVisitPlaceDto.coordinates?.let { mapToCoordinates(it) },
            notes = mustVisitPlaceDto.notes,
            source = mustVisitPlaceDto.source,
            openingHours = null, // MustVisitPlaceDto doesn't have openingHours
            image = mustVisitPlaceDto.image
        )
    }
    
    private fun mapToMustVisitPlace(mustVisitPlaceDetailDto: MustVisitPlaceDetailDto): MustVisitPlace {
        return MustVisitPlace(
            placeId = mustVisitPlaceDetailDto.placeId,
            placeName = mustVisitPlaceDetailDto.placeName,
            address = mustVisitPlaceDetailDto.address,
            coordinates = mustVisitPlaceDetailDto.coordinates?.let { mapToCoordinates(it) },
            notes = mustVisitPlaceDetailDto.notes,
            source = mustVisitPlaceDetailDto.source,
            openingHours = mustVisitPlaceDetailDto.openingHours,
            image = mustVisitPlaceDetailDto.image
        )
    }
    
    private fun mapToRouteDayDto(routeDay: RouteDay): RouteDayDto {
        return RouteDayDto(
            date = routeDay.date,
            activities = routeDay.activities.map { mapToActivityDto(it) }
        )
    }
    
    private fun mapToRouteDay(routeDayDto: RouteDayDto): RouteDay {
        return RouteDay(
            date = routeDayDto.date,
            activities = routeDayDto.activities.map { mapToActivity(it) }
        )
    }
    
    private fun mapToActivityDto(activity: Activity): ActivityDto {
        return ActivityDto(
            placeId = activity.placeId,
            placeName = activity.placeName,
            time = activity.time,
            notes = activity.notes,
            image = activity.image
        )
    }
    
    private fun mapToActivity(activityDto: ActivityDto): Activity {
        return Activity(
            placeId = activityDto.placeId,
            placeName = activityDto.placeName,
            time = activityDto.time,
            notes = activityDto.notes,
            image = activityDto.image
        )
    }
    
    private fun mapToCoordinatesDto(coordinates: Coordinates): CoordinatesDto {
        return CoordinatesDto(
            lat = coordinates.lat,
            lng = coordinates.lng
        )
    }
    
    private fun mapToCoordinates(coordinatesDto: CoordinatesDto): Coordinates {
        return Coordinates(
            lat = coordinatesDto.lat,
            lng = coordinatesDto.lng
        )
    }
    
    private fun mapToRouteStats(routeStatsDto: RouteStatsDto): RouteStats {
        return RouteStats(
            viewsCount = routeStatsDto.viewsCount,
            copiesCount = routeStatsDto.copiesCount,
            likesCount = routeStatsDto.likesCount
        )
    }
}