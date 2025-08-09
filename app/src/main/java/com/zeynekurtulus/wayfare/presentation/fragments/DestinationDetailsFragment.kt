package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentDestinationDetailsBinding
import com.zeynekurtulus.wayfare.domain.model.TopRatedPlace
import com.zeynekurtulus.wayfare.presentation.navigation.BottomNavigationHandler
import com.zeynekurtulus.wayfare.utils.showToast

/**
 * DestinationDetailsFragment - Displays comprehensive destination information
 * 
 * Features:
 * - Destination overview with image and rating
 * - Category and location information
 * - Description and details
 * - Action buttons (Plan Trip, View on Map)
 * - Opening hours and address information
 */
class DestinationDetailsFragment : Fragment() {

    private var _binding: FragmentDestinationDetailsBinding? = null
    private val binding get() = _binding!!

    private var destination: TopRatedPlace? = null

    companion object {
        private const val ARG_DESTINATION = "destination"

        fun newInstance(destination: TopRatedPlace): DestinationDetailsFragment {
            return DestinationDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_DESTINATION, destination)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDestinationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadDestinationData()
    }

    private fun setupUI() {
        setupToolbar()
        setupActionButtons()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            Log.d("DestinationDetailsFragment", "Back button clicked")
            parentFragmentManager.popBackStack()
        }

        // Heart icon removed as per user request
    }

    private fun setupActionButtons() {
        binding.planTripButton.setOnClickListener {
            planTripToDestination()
        }

        // View on map button removed as per user request
    }

    private fun loadDestinationData() {
        arguments?.let { args ->
            destination = args.getParcelable(ARG_DESTINATION)
            destination?.let { dest ->
                displayDestinationDetails(dest)
            } ?: run {
                Log.e("DestinationDetailsFragment", "No destination data provided")
                showToast("No destination data available")
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun displayDestinationDetails(destination: TopRatedPlace) {
        binding.apply {
            // Basic info
            destinationNameText.text = destination.name
            locationText.text = destination.address ?: "${destination.city}, ${destination.country ?: ""}"
            
            // Rating
            val rating = destination.rating ?: destination.wayfareRating ?: 0.0
            ratingText.text = String.format("%.1f", rating)
            
            // Category
            categoryChip.text = destination.category ?: destination.wayfareCategory
            
            // Description
            descriptionText.text = generateDescription(destination)
            
            // Opening hours
            openingHoursText.text = getOpeningHours(destination)
            
            // Address
            addressText.text = destination.address ?: "Address not available"
            
            // Load image
            loadDestinationImage(destination)
        }
    }

    private fun loadDestinationImage(destination: TopRatedPlace) {
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_map_placeholder)
            .error(R.drawable.ic_map_placeholder)

        Glide.with(this)
            .load(destination.image)
            .apply(requestOptions)
            .into(binding.destinationImageView)
    }

    private fun generateDescription(destination: TopRatedPlace): String {
        // Generate a basic description based on available data
        val category = destination.category ?: destination.wayfareCategory
        val location = destination.city
        
        return when {
            category?.contains("Historical", ignoreCase = true) == true -> {
                "Discover the rich history and cultural significance of ${destination.name} in $location. This historical landmark offers visitors a unique glimpse into the past with its remarkable architecture and fascinating stories."
            }
            category?.contains("Museum", ignoreCase = true) == true -> {
                "Explore the fascinating exhibits and collections at ${destination.name}. This renowned museum in $location showcases incredible artifacts and offers an educational experience for visitors of all ages."
            }
            category?.contains("Palace", ignoreCase = true) == true -> {
                "Step into the grandeur of ${destination.name}, a magnificent palace that reflects the architectural brilliance and royal heritage of $location. Experience the luxury and history of bygone eras."
            }
            category?.contains("Mosque", ignoreCase = true) == true -> {
                "Visit the beautiful ${destination.name}, an architectural masterpiece and important religious site in $location. Admire the stunning design and peaceful atmosphere of this sacred place."
            }
            category?.contains("Beach", ignoreCase = true) == true -> {
                "Relax and unwind at ${destination.name}, a beautiful beach destination in $location. Enjoy pristine waters, golden sands, and the perfect setting for a memorable getaway."
            }
            category?.contains("Park", ignoreCase = true) == true -> {
                "Escape to nature at ${destination.name}, a beautiful park in $location. Perfect for outdoor activities, peaceful walks, and enjoying the natural beauty of the area."
            }
            else -> {
                "Experience the unique charm of ${destination.name} in $location. This popular destination offers visitors an unforgettable experience with its distinctive character and local attractions."
            }
        }
    }

    private fun getOpeningHours(destination: TopRatedPlace): String {
        return destination.openingHours?.let { hours ->
            // Try to get today's hours or general hours
            val today = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                .format(java.util.Date()).lowercase()
            
            hours[today] ?: hours["general"] ?: hours.values.firstOrNull() ?: "Hours not available"
        } ?: "Hours not available"
    }

    private fun planTripToDestination() {
        destination?.let { dest ->
            Log.d("DestinationDetailsFragment", "Planning trip to: ${dest.name}")
            
            // Navigate to Trip Maker tab with this destination pre-selected
            showToast("Creating trip to ${dest.name}...")
            
            val activity = requireActivity()
            if (activity is com.zeynekurtulus.wayfare.presentation.activities.MainActivity) {
                activity.switchToTripMaker()
            }
        }
    }

    private fun viewOnMap() {
        destination?.let { dest ->
            Log.d("DestinationDetailsFragment", "Viewing ${dest.name} on map")
            showToast("Map view coming soon!")
            // TODO: Implement map view or external map integration
        }
    }

    private fun toggleFavorite() {
        destination?.let { dest ->
            Log.d("DestinationDetailsFragment", "Toggling favorite for: ${dest.name}")
            showToast("Favorites feature coming soon!")
            // TODO: Implement favorites functionality
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}