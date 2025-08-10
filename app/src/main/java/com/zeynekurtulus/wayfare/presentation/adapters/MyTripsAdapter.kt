package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.ItemTripDetailedBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * MyTripsAdapter - RecyclerView adapter for trips list/grid
 * 
 * Features:
 * - Supports both list and grid layouts
 * - Trip status calculation and display
 * - Privacy indicators
 * - Click handling for trips and menu
 * - Efficient updates with DiffUtil
 */
class MyTripsAdapter(
    private var isGridLayout: Boolean,
    private val onTripClick: (Route) -> Unit,
    private val onMenuClick: (Route, android.view.View) -> Unit
) : RecyclerView.Adapter<MyTripsAdapter.TripViewHolder>() {

    private var trips: List<Route> = emptyList()

    fun updateTrips(newTrips: List<Route>) {
        val diffCallback = TripDiffCallback(trips, newTrips)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        trips = newTrips
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateLayoutMode(isGrid: Boolean) {
        if (isGridLayout != isGrid) {
            isGridLayout = isGrid
            notifyDataSetChanged() // Full refresh needed for layout change
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripDetailedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    inner class TripViewHolder(
        private val binding: ItemTripDetailedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Route) {
            binding.apply {
                // Set trip title
                tripTitleText.text = trip.title
                
                // Set destination (use city from activities if available)
                destinationText.text = getDestinationText(trip)
                
                // Set dates
                datesText.text = formatDateRange(trip.startDate, trip.endDate)
                
                // Set duration
                durationText.text = "${calculateDuration(trip)} days"
                
                // Set budget info (if available in your model)
                budgetText.text = getBudgetText(trip)
                
                // Set category
                categoryChip.text = getCategoryText(trip)
                
                // Set trip status
                setupTripStatus(trip)
                
                // Set privacy indicator
                setupPrivacyIndicator(trip)
                
                // Load trip image
                loadTripImage(trip)
                
                // Set click listeners
                root.setOnClickListener {
                    onTripClick(trip)
                }
                
                menuButton.setOnClickListener { view ->
                    onMenuClick(trip, view)
                }
            }
        }

        private fun setupTripStatus(trip: Route) {
            val status = getTripStatus(trip)
            val (statusText, backgroundColor, textColor) = when (status) {
                TripStatus.UPCOMING -> Triple(
                    "Upcoming",
                    R.color.primary_blue_50,
                    R.color.primary_blue_700
                )
                TripStatus.ONGOING -> Triple(
                    "Ongoing",
                    R.color.secondary_green,
                    R.color.white
                )
                TripStatus.COMPLETED -> Triple(
                    "Completed",
                    R.color.grey_100,
                    R.color.text_secondary
                )
            }

            binding.statusChip.apply {
                text = statusText
                setChipBackgroundColorResource(backgroundColor)
                setTextColor(binding.root.context.getColor(textColor))
            }
        }

        private fun setupPrivacyIndicator(trip: Route) {
            binding.privacyIndicator.apply {
                if (trip.isPublic) {
                    setImageResource(R.drawable.ic_public)
                    setColorFilter(binding.root.context.getColor(R.color.secondary_green))
                } else {
                    setImageResource(R.drawable.ic_lock_private)
                    setColorFilter(binding.root.context.getColor(R.color.text_hint))
                }
            }
        }

        private fun loadTripImage(trip: Route) {
            val imageUrl = getTripImageUrl(trip)
            
            val requestOptions = RequestOptions()
                .transform(RoundedCorners(8))
                .placeholder(R.drawable.ic_map_placeholder)
                .error(R.drawable.ic_map_placeholder)

            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.tripImageView)
        }

        private fun getDestinationText(trip: Route): String {
            // Use the city from the route
            return "${trip.city}, ${trip.country}"
        }

        private fun formatDateRange(startDate: String, endDate: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                
                val start = inputFormat.parse(startDate)
                val end = inputFormat.parse(endDate)
                
                if (start != null && end != null) {
                    val startFormatted = outputFormat.format(start)
                    val endFormatted = outputFormat.format(end)
                    val year = yearFormat.format(end)
                    
                    "$startFormatted - $endFormatted, $year"
                } else {
                    "$startDate - $endDate"
                }
            } catch (e: Exception) {
                "$startDate - $endDate"
            }
        }

        private fun calculateDuration(trip: Route): Int {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = sdf.parse(trip.startDate)
                val endDate = sdf.parse(trip.endDate)
                
                if (startDate != null && endDate != null) {
                    val diffInMillis = endDate.time - startDate.time
                    (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
                } else {
                    1
                }
            } catch (e: Exception) {
                1
            }
        }

        private fun getBudgetText(trip: Route): String {
            // Use the budget from the route
            return trip.budget
        }

        private fun getCategoryText(trip: Route): String {
            // Use the category from the route
            return trip.category
        }

        private fun getTripImageUrl(trip: Route): String? {
            // Get first activity image if available from the first day
            return trip.days.firstOrNull()?.activities?.firstOrNull()?.image
        }

        private fun getTripStatus(trip: Route): TripStatus {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = sdf.parse(trip.startDate)
                val endDate = sdf.parse(trip.endDate)
                val today = Calendar.getInstance().time
                
                when {
                    startDate != null && endDate != null -> {
                        when {
                            today.before(startDate) -> TripStatus.UPCOMING
                            today.after(endDate) -> TripStatus.COMPLETED
                            else -> TripStatus.ONGOING
                        }
                    }
                    else -> TripStatus.UPCOMING
                }
            } catch (e: Exception) {
                TripStatus.UPCOMING
            }
        }
    }

    enum class TripStatus {
        UPCOMING, ONGOING, COMPLETED
    }

    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    private class TripDiffCallback(
        private val oldList: List<Route>,
        private val newList: List<Route>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].routeId == newList[newItemPosition].routeId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return oldItem.title == newItem.title &&
                   oldItem.startDate == newItem.startDate &&
                   oldItem.endDate == newItem.endDate &&
                   oldItem.isPublic == newItem.isPublic
        }
    }
}