package com.zeynekurtulus.wayfare.data.mappers

import com.google.gson.Gson
import com.zeynekurtulus.wayfare.data.local.entities.RouteEntity
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.RouteDetail

object RouteEntityMapper {
    
    private val gson = Gson()
    
    /**
     * Convert Route domain model to RouteEntity for database storage
     */
    fun fromRoute(route: Route, isDownloaded: Boolean = false): RouteEntity {
        return RouteEntity(
            routeId = route.routeId,
            userId = route.userId,
            title = route.title,
            city = route.city,
            cityId = route.cityId,
            country = route.country,
            countryId = route.countryId,
            startDate = route.startDate,
            endDate = route.endDate,
            budget = route.budget,
            travelStyle = route.travelStyle,
            category = route.category,
            season = route.season,
            statsJson = gson.toJson(route.stats),
            mustVisitJson = gson.toJson(route.mustVisit),
            daysJson = gson.toJson(route.days),
            createdAt = route.createdAt,
            updatedAt = route.updatedAt,
            isPublic = route.isPublic,
            isDownloaded = isDownloaded,
            downloadedAt = if (isDownloaded) System.currentTimeMillis() else null,
            lastSyncedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Convert RouteDetail domain model to RouteEntity for database storage
     */
    fun fromRouteDetail(routeDetail: RouteDetail, isDownloaded: Boolean = false): RouteEntity {
        return RouteEntity(
            routeId = routeDetail.routeId,
            userId = routeDetail.userId,
            title = routeDetail.title,
            city = routeDetail.city,
            cityId = routeDetail.cityId,
            country = routeDetail.country,
            countryId = routeDetail.countryId,
            startDate = routeDetail.startDate,
            endDate = routeDetail.endDate,
            budget = routeDetail.budget,
            travelStyle = routeDetail.travelStyle,
            category = routeDetail.category,
            season = routeDetail.season,
            statsJson = gson.toJson(routeDetail.stats),
            mustVisitJson = gson.toJson(routeDetail.mustVisit),
            daysJson = gson.toJson(routeDetail.days),
            createdAt = routeDetail.createdAt,
            updatedAt = routeDetail.updatedAt,
            isPublic = routeDetail.isPublic,
            isDownloaded = isDownloaded,
            downloadedAt = if (isDownloaded) System.currentTimeMillis() else null,
            lastSyncedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Convert RouteEntity back to Route domain model
     */
    fun toRoute(entity: RouteEntity): Route {
        return Route(
            routeId = entity.routeId,
            userId = entity.userId,
            title = entity.title,
            city = entity.city,
            cityId = entity.cityId,
            country = entity.country,
            countryId = entity.countryId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            budget = entity.budget,
            travelStyle = entity.travelStyle,
            category = entity.category,
            season = entity.season,
            stats = gson.fromJson(entity.statsJson, com.zeynekurtulus.wayfare.domain.model.RouteStats::class.java),
            mustVisit = gson.fromJson(entity.mustVisitJson, Array<com.zeynekurtulus.wayfare.domain.model.MustVisitPlace>::class.java).toList(),
            days = gson.fromJson(entity.daysJson, Array<com.zeynekurtulus.wayfare.domain.model.RouteDay>::class.java).toList(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPublic = entity.isPublic
        )
    }
    
    /**
     * Convert RouteEntity to RouteDetail domain model
     */
    fun toRouteDetail(entity: RouteEntity): RouteDetail {
        return RouteDetail(
            routeId = entity.routeId,
            userId = entity.userId,
            title = entity.title,
            city = entity.city,
            cityId = entity.cityId,
            country = entity.country,
            countryId = entity.countryId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            budget = entity.budget,
            travelStyle = entity.travelStyle,
            category = entity.category,
            season = entity.season,
            stats = gson.fromJson(entity.statsJson, com.zeynekurtulus.wayfare.domain.model.RouteStats::class.java),
            mustVisit = gson.fromJson(entity.mustVisitJson, Array<com.zeynekurtulus.wayfare.domain.model.MustVisitPlace>::class.java).toList(),
            days = gson.fromJson(entity.daysJson, Array<com.zeynekurtulus.wayfare.domain.model.RouteDay>::class.java).toList(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPublic = entity.isPublic
        )
    }
    
    /**
     * Check if a route is downloaded for offline use
     */
    fun isRouteDownloaded(entity: RouteEntity): Boolean {
        return entity.isDownloaded
    }
    
    /**
     * Get download timestamp
     */
    fun getDownloadTime(entity: RouteEntity): Long? {
        return entity.downloadedAt
    }
}