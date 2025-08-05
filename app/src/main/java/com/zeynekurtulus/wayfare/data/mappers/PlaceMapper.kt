package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.place.*
import com.zeynekurtulus.wayfare.domain.model.*

object PlaceMapper {
    
    fun mapToPlacesByIdsRequest(placeIds: List<String>): PlacesByIdsRequest {
        return PlacesByIdsRequest(placeIds = placeIds)
    }
    
    fun mapToSearchPlacesRequest(searchPlaces: SearchPlaces): SearchPlacesRequest {
        return SearchPlacesRequest(
            query = searchPlaces.query,
            city = searchPlaces.city,
            category = searchPlaces.category,
            limit = searchPlaces.limit
        )
    }
    
    fun mapToAutocompletePlacesRequest(autocompletePlaces: AutocompletePlaces): AutocompletePlacesRequest {
        return AutocompletePlacesRequest(
            query = autocompletePlaces.query,
            city = autocompletePlaces.city,
            limit = autocompletePlaces.limit
        )
    }
    
    fun mapToPlace(placeDto: PlaceDto): Place {
        return Place(
            placeId = placeDto.placeId,
            name = placeDto.name,
            address = placeDto.address,
            coordinates = placeDto.coordinates?.let { mapToCoordinates(it) },
            category = placeDto.category,
            rating = placeDto.rating,
            priceLevel = placeDto.priceLevel,
            openingHours = placeDto.openingHours,
            image = placeDto.image,
            detailUrl = placeDto.detailUrl,
            duration = placeDto.duration
        )
    }
    
    fun mapToAutocompletePlace(autocompletePlaceDto: AutocompletePlaceDto): AutocompletePlace {
        return AutocompletePlace(
            placeId = autocompletePlaceDto.placeId,
            name = autocompletePlaceDto.name,
            category = autocompletePlaceDto.category
        )
    }
    
    fun mapToTopRatedPlace(topRatedDto: TopRatedDto): TopRatedPlace {
        return TopRatedPlace(
            placeId = topRatedDto.placeId,
            name = topRatedDto.name,
            city = topRatedDto.city,
            category = topRatedDto.category,
            wayfareCategory = topRatedDto.wayfareCategory,
            price = topRatedDto.price,
            rating = topRatedDto.rating,
            wayfareRating = topRatedDto.wayfareRating,
            totalFeedbackCount = topRatedDto.totalFeedbackCount,
            image = topRatedDto.image,
            detailUrl = topRatedDto.detailUrl,
            openingHours = topRatedDto.openingHours,
            coordinates = topRatedDto.coordinates?.let { mapToCoordinates(it) },
            address = topRatedDto.address,
            source = topRatedDto.source,
            country = topRatedDto.country,
            countryId = topRatedDto.countryId,
            cityId = topRatedDto.cityId,
            popularity = topRatedDto.popularity,
            duration = topRatedDto.duration,
            createdAt = topRatedDto.createdAt,
            updatedAt = topRatedDto.updatedAt
        )
    }
    
    private fun mapToCoordinates(coordinatesDto: com.zeynekurtulus.wayfare.data.api.dto.route.CoordinatesDto): Coordinates {
        return Coordinates(
            lat = coordinatesDto.lat,
            lng = coordinatesDto.lng
        )
    }
}