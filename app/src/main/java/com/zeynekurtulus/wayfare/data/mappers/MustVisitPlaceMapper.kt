package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.place.MustVisitPlaceSearchDto
import com.zeynekurtulus.wayfare.data.api.dto.place.PlaceCoordinatesDto
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlaceSearch
import com.zeynekurtulus.wayfare.domain.model.PlaceCoordinates
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlace
import com.zeynekurtulus.wayfare.domain.model.Coordinates

/**
 * Mapper for converting between must-visit place DTOs and domain models
 */
object MustVisitPlaceMapper {
    
    fun mapToMustVisitPlaceSearch(dto: MustVisitPlaceSearchDto): MustVisitPlaceSearch {
        return MustVisitPlaceSearch(
            placeId = dto.placeId,
            name = dto.name,
            category = dto.category,
            wayfareCategory = dto.wayfareCategory,
            rating = dto.rating,
            image = dto.image,
            coordinates = dto.coordinates?.let { mapToPlaceCoordinates(it) } ?: PlaceCoordinates(0.0, 0.0),
            address = dto.address ?: "Address not available",
            isSelected = false
        )
    }
    
    fun mapToMustVisitPlaceSearchList(dtos: List<MustVisitPlaceSearchDto>): List<MustVisitPlaceSearch> {
        return dtos.map { mapToMustVisitPlaceSearch(it) }
    }
    
    private fun mapToPlaceCoordinates(dto: PlaceCoordinatesDto): PlaceCoordinates {
        return PlaceCoordinates(
            lat = dto.lat,
            lng = dto.lng
        )
    }
    
    /**
     * Convert search result to MustVisitPlace for route creation
     */
    fun mapToMustVisitPlace(searchResult: MustVisitPlaceSearch, notes: String = ""): MustVisitPlace {
        return MustVisitPlace(
            placeId = searchResult.placeId,
            placeName = searchResult.name,
            address = searchResult.address,
            coordinates = Coordinates(
                lat = searchResult.coordinates.lat,
                lng = searchResult.coordinates.lng
            ),
            notes = notes,
            source = "database",
            openingHours = null,
            image = null
        )
    }
}