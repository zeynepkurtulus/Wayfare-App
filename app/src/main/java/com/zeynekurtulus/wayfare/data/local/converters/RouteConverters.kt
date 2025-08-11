package com.zeynekurtulus.wayfare.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zeynekurtulus.wayfare.domain.model.MustVisitPlace
import com.zeynekurtulus.wayfare.domain.model.RouteDay
import com.zeynekurtulus.wayfare.domain.model.RouteStats

class RouteConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromRouteStats(stats: RouteStats): String {
        return gson.toJson(stats)
    }

    @TypeConverter
    fun toRouteStats(json: String): RouteStats {
        return gson.fromJson(json, RouteStats::class.java)
    }

    @TypeConverter
    fun fromMustVisitPlaceList(places: List<MustVisitPlace>): String {
        return gson.toJson(places)
    }

    @TypeConverter
    fun toMustVisitPlaceList(json: String): List<MustVisitPlace> {
        val type = object : TypeToken<List<MustVisitPlace>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromRouteDayList(days: List<RouteDay>): String {
        return gson.toJson(days)
    }

    @TypeConverter
    fun toRouteDayList(json: String): List<RouteDay> {
        val type = object : TypeToken<List<RouteDay>>() {}.type
        return gson.fromJson(json, type)
    }
}