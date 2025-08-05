package com.zeynekurtulus.wayfare.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private val apiDateFormat = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
    private val apiTimeFormat = SimpleDateFormat(Constants.TIME_FORMAT_API, Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault())
    
    fun formatDateForApi(date: Date): String {
        return apiDateFormat.format(date)
    }
    
    fun formatDateForDisplay(date: Date): String {
        return displayDateFormat.format(date)
    }
    
    fun formatTimeForApi(date: Date): String {
        return apiTimeFormat.format(date)
    }
    
    fun formatTimeForDisplay(date: Date): String {
        return displayTimeFormat.format(date)
    }
    
    fun parseDateFromApi(dateString: String): Date? {
        return try {
            apiDateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun parseTimeFromApi(timeString: String): Date? {
        return try {
            apiTimeFormat.parse(timeString)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentDate(): String {
        return formatDateForApi(Date())
    }
    
    fun getCurrentTime(): String {
        return formatTimeForApi(Date())
    }
    
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }
    
    fun daysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillies = endDate.time - startDate.time
        return (diffInMillies / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun isValidDateRange(startDate: String, endDate: String): Boolean {
        return try {
            val start = parseDateFromApi(startDate)
            val end = parseDateFromApi(endDate)
            start != null && end != null && !end.before(start)
        } catch (e: Exception) {
            false
        }
    }
    
    fun isFutureDate(dateString: String): Boolean {
        return try {
            val date = parseDateFromApi(dateString)
            date != null && date.after(Date())
        } catch (e: Exception) {
            false
        }
    }
    
    fun formatDateRange(startDate: String, endDate: String): String {
        val start = parseDateFromApi(startDate)
        val end = parseDateFromApi(endDate)
        
        return if (start != null && end != null) {
            "${formatDateForDisplay(start)} - ${formatDateForDisplay(end)}"
        } else {
            "$startDate - $endDate"
        }
    }
}