package com.zeynekurtulus.wayfare.data.mappers

import com.zeynekurtulus.wayfare.data.api.dto.location.*
import com.zeynekurtulus.wayfare.domain.model.*

object LocationMapper {
    
    fun mapToCitiesByCountryRequest(country: String): CitiesByCountryRequest {
        return CitiesByCountryRequest(country = country)
    }
    
    fun mapToCountriesByRegionRequest(region: String): CountriesByRegionRequest {
        return CountriesByRegionRequest(region = region)
    }
    
    fun mapToSearchCountriesRequest(searchCountries: SearchCountries): SearchCountriesRequest {
        return SearchCountriesRequest(
            query = searchCountries.query,
            limit = searchCountries.limit
        )
    }
    
    fun mapToLocationCity(cityDto: CityDto): LocationCity {
        return LocationCity(
            cityId = cityDto.cityId,
            name = cityDto.name,
            country = cityDto.country,
            countryId = cityDto.countryId,
            active = cityDto.active,
            coordinates = cityDto.coordinates?.let { mapToCoordinates(it) },
            timezone = cityDto.timezone,
            createdAt = cityDto.createdAt,
            updatedAt = cityDto.updatedAt
        )
    }
    
    fun mapToCountry(countryDto: CountryDto): Country {
        return Country(
            name = countryDto.name,
            countryId = countryDto.countryId,
            region = countryDto.region,
            active = countryDto.active,
            createdAt = countryDto.createdAt,
            updatedAt = countryDto.updatedAt
        )
    }
    
    fun mapToCountryFromSimple(simpleCountryDto: SimpleCountryDto): Country {
        return Country(
            name = simpleCountryDto.name,
            countryId = simpleCountryDto.id,
            region = simpleCountryDto.region,
            active = true,
            createdAt = "",
            updatedAt = ""
        )
    }
    
    private fun mapToCoordinates(coordinatesDto: com.zeynekurtulus.wayfare.data.api.dto.route.CoordinatesDto): Coordinates {
        return Coordinates(
            lat = coordinatesDto.lat,
            lng = coordinatesDto.lng
        )
    }
}