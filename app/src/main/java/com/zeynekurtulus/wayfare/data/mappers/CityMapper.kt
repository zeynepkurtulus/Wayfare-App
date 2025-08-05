package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.city.CityDto
import com.zeynekurtulus.wayfare.data.api.dto.city.CoordinatesDto
import com.zeynekurtulus.wayfare.domain.model.City
import com.zeynekurtulus.wayfare.domain.model.CityCoordinates

object CityMapper {
    
    fun mapToCity(cityDto: CityDto): City {
        return City(
            cityId = cityDto.cityId,
            name = cityDto.name,
            country = cityDto.country,
            countryId = cityDto.countryId,
            displayText = cityDto.displayText,
            coordinates = mapToCoordinates(cityDto.coordinates)
        )
    }
    
    private fun mapToCoordinates(coordinatesDto: CoordinatesDto): CityCoordinates {
        return CityCoordinates(
            lat = coordinatesDto.lat,
            lng = coordinatesDto.lng
        )
    }
    
    fun mapToCityList(cityDtos: List<CityDto>): List<City> {
        return cityDtos.map { mapToCity(it) }
    }
}