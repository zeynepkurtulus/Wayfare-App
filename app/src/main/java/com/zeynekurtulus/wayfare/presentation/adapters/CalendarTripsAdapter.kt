package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.domain.model.Route
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CalendarTripsAdapter(
    private var trips: List<Route>,
    private val onTripClick: (Route) -> Unit
) : RecyclerView.Adapter<CalendarTripsAdapter.CalendarTripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarTripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_trip, parent, false)
        return CalendarTripViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarTripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int = trips.size

    fun updateTrips(newTrips: List<Route>) {
        trips = newTrips
        notifyDataSetChanged()
    }

    inner class CalendarTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripImageView: CircleImageView = itemView.findViewById(R.id.tripImageView)
        private val tripTitleTextView: TextView = itemView.findViewById(R.id.tripTitleTextView)
        private val tripLocationTextView: TextView = itemView.findViewById(R.id.tripLocationTextView)
        private val tripDatesTextView: TextView = itemView.findViewById(R.id.tripDatesTextView)
        private val tripStatusTextView: TextView = itemView.findViewById(R.id.tripStatusTextView)
        private val tripDurationTextView: TextView = itemView.findViewById(R.id.tripDurationTextView)
        private val tripProgressIndicator: LinearProgressIndicator = itemView.findViewById(R.id.tripProgressIndicator)
        private val privacyIndicator: ImageView = itemView.findViewById(R.id.privacyIndicator)

        fun bind(trip: Route) {
            // Set trip details
            tripTitleTextView.text = trip.title
            tripLocationTextView.text = trip.city

            // Format dates
            try {
                val startDate = LocalDate.parse(trip.startDate)
                val endDate = LocalDate.parse(trip.endDate)
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
                val datesText = if (startDate.year == endDate.year && startDate.month == endDate.month) {
                    "${dateFormatter.format(startDate)} - ${endDate.dayOfMonth}"
                } else {
                    "${dateFormatter.format(startDate)} - ${dateFormatter.format(endDate)}"
                }
                tripDatesTextView.text = datesText

                // Calculate duration
                val duration = ChronoUnit.DAYS.between(startDate, endDate) + 1
                tripDurationTextView.text = "$duration day${if (duration > 1) "s" else ""}"

                // Determine trip status and progress
                val currentDate = LocalDate.now()
                when {
                    endDate.isBefore(currentDate) -> {
                        // Past trip
                        tripStatusTextView.text = "Completed"
                        tripStatusTextView.setBackgroundResource(R.drawable.rounded_corners_8dp)
                        tripStatusTextView.setBackgroundColor(
                            itemView.context.getColor(R.color.grey_500)
                        )
                        tripProgressIndicator.visibility = View.GONE
                    }
                    startDate.isAfter(currentDate) -> {
                        // Future trip
                        tripStatusTextView.text = "Upcoming"
                        tripStatusTextView.setBackgroundResource(R.drawable.rounded_corners_8dp)
                        tripStatusTextView.setBackgroundColor(
                            itemView.context.getColor(R.color.success_green)
                        )
                        tripProgressIndicator.visibility = View.GONE
                    }
                    else -> {
                        // Ongoing trip
                        tripStatusTextView.text = "Ongoing"
                        tripStatusTextView.setBackgroundResource(R.drawable.rounded_corners_8dp)
                        tripStatusTextView.setBackgroundColor(
                            itemView.context.getColor(R.color.primary_blue_700)
                        )
                        
                        // Calculate and show progress
                        val totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
                        val elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate) + 1
                        val progress = ((elapsedDays.toFloat() / totalDays.toFloat()) * 100).toInt()
                        
                        tripProgressIndicator.progress = progress
                        tripProgressIndicator.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                // Fallback for date parsing errors
                tripDatesTextView.text = "${trip.startDate} - ${trip.endDate}"
                tripDurationTextView.text = "Multi-day"
                tripStatusTextView.text = "Planned"
                tripProgressIndicator.visibility = View.GONE
            }

            // Load trip image (first image from route activities)
            val tripImageUrl = getFirstImageFromRoute(trip)
            if (tripImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(tripImageUrl)
                    .placeholder(R.drawable.ic_map_placeholder)
                    .error(R.drawable.ic_map_placeholder)
                    .into(tripImageView)
            } else {
                tripImageView.setImageResource(R.drawable.ic_map_placeholder)
            }

            // Set privacy indicator
            if (trip.isPublic) {
                privacyIndicator.setImageResource(R.drawable.ic_public)
                privacyIndicator.setColorFilter(itemView.context.getColor(R.color.success_green))
            } else {
                privacyIndicator.setImageResource(R.drawable.ic_lock_private)
                privacyIndicator.setColorFilter(itemView.context.getColor(R.color.text_hint))
            }

            // Set click listener
            itemView.setOnClickListener {
                onTripClick(trip)
            }
        }

        private fun getFirstImageFromRoute(route: Route): String {
            // Try to get image from the first day's first activity
            if (route.days.isNotEmpty()) {
                val firstDay = route.days[0]
                if (firstDay.activities.isNotEmpty()) {
                    val firstActivity = firstDay.activities[0]
                    if (firstActivity.image?.isNotEmpty() == true) {
                        return firstActivity.image!!
                    }
                }
            }
            return ""
        }
    }
}