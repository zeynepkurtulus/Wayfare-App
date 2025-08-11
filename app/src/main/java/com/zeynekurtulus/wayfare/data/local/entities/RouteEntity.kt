package com.zeynekurtulus.wayfare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.zeynekurtulus.wayfare.data.local.converters.RouteConverters

@Entity(tableName = "routes")
@TypeConverters(RouteConverters::class)
data class RouteEntity(
    @PrimaryKey
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
    val statsJson: String, // JSON string for RouteStats
    val mustVisitJson: String, // JSON string for List<MustVisitPlace>
    val daysJson: String, // JSON string for List<RouteDay>
    val createdAt: String?,
    val updatedAt: String?,
    val isPublic: Boolean = false,
    val isDownloaded: Boolean = false, // Track if route is downloaded for offline use
    val downloadedAt: Long? = null, // Timestamp when downloaded
    val lastSyncedAt: Long? = null // Last time synced with server
)