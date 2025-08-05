package com.zeynekurtulus.wayfare.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationCity(
    val cityId: String,
    val name: String,
    val country: String,
    val countryId: String,
    val active: Boolean,
    val coordinates: Coordinates?,
    val timezone: String?,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

@Parcelize
data class Country(
    val name: String,
    val countryId: String,
    val region: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

data class SearchCountries(
    val query: String,
    val limit: Int = 10
)