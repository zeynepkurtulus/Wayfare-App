package com.zeynekurtulus.wayfare.presentation.calendar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.domain.model.Route
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class CustomCalendarView(
    private val context: Context,
    private val calendarGridContainer: LinearLayout,
    private val monthYearTextView: TextView,
    private val onDateSelected: (LocalDate) -> Unit
) {
    
    private var currentMonth: YearMonth = YearMonth.now()
    private var selectedDate: LocalDate? = null
    private var tripDates: Map<LocalDate, List<Route>> = emptyMap()
    
    // Trip colors - cycling through different colors for different trips
    private val tripColors = listOf(
        R.color.primary_blue_700,  // Blue
        R.color.secondary_green,   // Green
        R.color.accent_orange,     // Orange
        R.color.primary,           // Primary
        R.color.error             // Red
    )
    
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    
    fun setCurrentMonth(yearMonth: YearMonth) {
        currentMonth = yearMonth
        updateCalendarDisplay()
    }
    
    fun setTripDates(routes: List<Route>) {
        android.util.Log.d("CustomCalendarView", "🎯 setTripDates called with ${routes.size} routes")
        
        // Group routes by dates they span
        val dateToRoutes = mutableMapOf<LocalDate, MutableList<Route>>()
        
        routes.forEach { route ->
            try {
                val startDate = LocalDate.parse(route.startDate)
                val endDate = LocalDate.parse(route.endDate)
                android.util.Log.d("CustomCalendarView", "📅 Processing route ${route.routeId}: $startDate to $endDate")
                
                var current = startDate
                while (!current.isAfter(endDate)) {
                    dateToRoutes.getOrPut(current) { mutableListOf() }.add(route)
                    current = current.plusDays(1)
                }
            } catch (e: Exception) {
                android.util.Log.e("CustomCalendarView", "Error parsing dates for route: ${route.routeId}", e)
            }
        }
        
        tripDates = dateToRoutes
        android.util.Log.d("CustomCalendarView", "🗓️ Trip dates map has ${tripDates.size} entries")
        updateCalendarDisplay()
    }
    
    private fun updateCalendarDisplay() {
        monthYearTextView.text = currentMonth.format(monthFormatter)
        calendarGridContainer.removeAllViews()
        
        val firstOfMonth = currentMonth.atDay(1)
        val lastOfMonth = currentMonth.atEndOfMonth()
        
        // Get the first day of the week (Sunday = 1)
        val weekFields = WeekFields.of(Locale.getDefault())
        val firstDayOfWeek = weekFields.firstDayOfWeek
        
        // Calculate the starting date for the calendar grid
        var startDate = firstOfMonth
        while (startDate.dayOfWeek != firstDayOfWeek) {
            startDate = startDate.minusDays(1)
        }
        
        // Create 6 weeks of calendar
        var currentDate = startDate
        repeat(6) { weekIndex ->
            val weekRow = createWeekRow()
            
            repeat(7) { dayIndex ->
                val dayCell = createDayCell(currentDate)
                weekRow.addView(dayCell)
                currentDate = currentDate.plusDays(1)
            }
            
            calendarGridContainer.addView(weekRow)
            
            // Stop if we've shown all days of the current month and we're in a new month
            if (currentDate.month != currentMonth.month && weekIndex >= 3) {
                return@repeat
            }
        }
    }
    
    private fun createWeekRow(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }
    }
    
    private fun createDayCell(date: LocalDate): View {
        val inflater = LayoutInflater.from(context)
        val dayCell = inflater.inflate(R.layout.calendar_day_cell, null)
        
        val dayNumberTextView = dayCell.findViewById<TextView>(R.id.dayNumberTextView)
        val dayBackgroundCard = dayCell.findViewById<MaterialCardView>(R.id.dayBackgroundCard)
        val tripIndicatorsContainer = dayCell.findViewById<LinearLayout>(R.id.tripIndicatorsContainer)
        
        // Set day number
        dayNumberTextView.text = date.dayOfMonth.toString()
        
        // Style based on whether it's in current month
        val isCurrentMonth = date.month == currentMonth.month
        val isToday = date == LocalDate.now()
        val isSelected = date == selectedDate
        val tripsForDate = tripDates[date] ?: emptyList()
        
        // Set text color
        when {
            !isCurrentMonth -> {
                dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.text_disabled))
            }
            isToday -> {
                dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                dayBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue_700))
            }
            isSelected -> {
                dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                dayBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            }
            tripsForDate.isNotEmpty() -> {
                dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                dayBackgroundCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.accent_light))
            }
            else -> {
                dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
        }
        
        // Add trip indicator dots
        tripIndicatorsContainer.removeAllViews()
        if (tripsForDate.isNotEmpty()) {
            android.util.Log.d("CustomCalendarView", "🔴 Adding ${tripsForDate.size} dots for date $date")
        }
        tripsForDate.take(3).forEachIndexed { index, trip ->
            val dot = createTripDot(index)
            tripIndicatorsContainer.addView(dot)
        }
        
        // Set click listener
        dayCell.setOnClickListener {
            if (isCurrentMonth) {
                selectedDate = date
                onDateSelected(date)
                updateCalendarDisplay() // Refresh to show selection
            }
        }
        
        return dayCell
    }
    
    private fun createTripDot(tripIndex: Int): View {
        val dot = View(context)
        val size = context.resources.getDimensionPixelSize(R.dimen.trip_dot_size) // We'll need to define this
        val margin = context.resources.getDimensionPixelSize(R.dimen.trip_dot_margin) // We'll need to define this
        
        val layoutParams = LinearLayout.LayoutParams(size, size).apply {
            setMargins(margin, 0, margin, 0)
        }
        dot.layoutParams = layoutParams
        
        // Create circular background with trip color
        val colorIndex = tripIndex % tripColors.size
        val colorRes = tripColors[colorIndex]
        val color = ContextCompat.getColor(context, colorRes)
        
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        dot.background = drawable
        
        return dot
    }
    
    fun goToPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        updateCalendarDisplay()
    }
    
    fun goToNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        updateCalendarDisplay()
    }
    
    fun goToCurrentMonth() {
        currentMonth = YearMonth.now()
        updateCalendarDisplay()
    }
}