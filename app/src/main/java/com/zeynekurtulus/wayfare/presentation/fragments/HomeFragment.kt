package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeynekurtulus.wayfare.databinding.FragmentHomeBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.TopRatedPlace
import com.zeynekurtulus.wayfare.presentation.activities.Destination
import com.zeynekurtulus.wayfare.presentation.activities.Trip
import com.zeynekurtulus.wayfare.presentation.fragments.DestinationDetailsFragment
import com.zeynekurtulus.wayfare.presentation.fragments.TripDetailsFragment
import com.zeynekurtulus.wayfare.presentation.adapters.DestinationsAdapter
import com.zeynekurtulus.wayfare.presentation.adapters.TripsAdapter
import com.zeynekurtulus.wayfare.presentation.navigation.BottomNavigationHandler.NavigationTab
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.PlaceViewModel
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.SharedPreferencesManager
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import com.zeynekurtulus.wayfare.R

/**
 * HomeFragment - Main home screen showing destinations and trips
 * 
 * This fragment displays the main dashboard with:
 * - User greeting
 * - Top destinations section
 * - User's trips section
 */
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    
    // ViewModel for fetching user routes
    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    // ViewModel for fetching top rated places
    private val placeViewModel: PlaceViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    // Adapters
    private lateinit var destinationsAdapter: DestinationsAdapter
    
    private val sampleTrips = listOf(
        Trip("Paris Adventure", "https://example.com/paris_trip.jpg"),
        Trip("Tokyo Explorer", "https://example.com/tokyo_trip.jpg"),
        Trip("NYC Weekend", "https://example.com/nyc_trip.jpg"),
        Trip("London Getaway", "https://example.com/london_trip.jpg"),
        Trip("Barcelona Vibes", "https://example.com/barcelona_trip.jpg"),
        Trip("Rome History Tour", "https://example.com/rome_trip.jpg")
    )
    
    // Adapters
    private lateinit var tripsAdapter: TripsAdapter
    private var userRoutes = listOf<Route>()
    
    // Store original data for navigation
    private var topRatedPlaces = listOf<TopRatedPlace>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize SharedPreferences
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        
        setupHomeScreen()
        setupClickListeners()
        setupObservers()
        // Note: fetchUserRoutes() removed - ViewModel calls loadUserRoutes() automatically in init
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "🔄 onResume: Refreshing user routes to show latest trips")
        // Refresh routes when user returns to home screen (e.g., after creating a trip)
        fetchUserRoutes()
    }
    
    private fun setupHomeScreen() {
        // Set user name from SharedPreferences
        val username = sharedPreferencesManager.getUsername() ?: "User"
        binding.userNameTextView.text = username
        
        // Setup destinations RecyclerView for horizontal scrolling
        val destinationsLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        destinationsAdapter = DestinationsAdapter(emptyList()) { destination ->
            onDestinationClicked(destination)
        }
        binding.destinationsRecyclerView.apply {
            layoutManager = destinationsLayoutManager
            adapter = destinationsAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
        
        // Setup trips RecyclerView for horizontal scrolling
        val tripsLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        tripsAdapter = TripsAdapter(emptyList()) { trip ->
            onTripClicked(trip)
        }
        binding.tripsRecyclerView.apply {
            layoutManager = tripsLayoutManager
            adapter = tripsAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }
    
    private fun setupClickListeners() {
        // View All buttons
        binding.viewAllDestinationsTextView.setOnClickListener {
            navigateToAllDestinations()
        }
        
        binding.viewAllTripsTextView.setOnClickListener {
            navigateToMyTrips()
        }
        
        // Empty state button click
        binding.startTripMakerButton.setOnClickListener {
            // Navigate to Trip Maker
            (activity as? androidx.appcompat.app.AppCompatActivity)?.let { activity ->
                if (activity is com.zeynekurtulus.wayfare.presentation.activities.MainActivity) {
                    activity.switchToTripMaker()
                }
            }
        }
        
        // DEBUG: Long press to refresh trips (for debugging API issues)
        binding.startTripMakerButton.setOnLongClickListener {
            Log.i("HomeFragment", "🔧 DEBUG: Long press detected - Manual refresh triggered by user")
            debugRefreshTrips()
            true
        }
    }
    
    private fun onDestinationClicked(destination: Destination) {
        Log.d("HomeFragment", "🎯 Destination clicked: ${destination.name}")
        
        // Find the original TopRatedPlace by matching the name
        val originalPlace = topRatedPlaces.find { place ->
            val expectedName = "${place.name}, ${place.city}"
            expectedName == destination.name
        }
        
        if (originalPlace != null) {
            Log.d("HomeFragment", "✅ Found matching TopRatedPlace: ${originalPlace.name}")
            navigateToDestinationDetails(originalPlace)
        } else {
            Log.e("HomeFragment", "❌ Could not find matching TopRatedPlace for: ${destination.name}")
            showToast("Unable to load destination details")
        }
    }
    
    private fun onTripClicked(trip: Trip) {
        Log.d("HomeFragment", "🧳 Trip clicked: ${trip.name}")
        
        // Find the original Route by matching the title
        val originalRoute = userRoutes.find { route ->
            route.title == trip.name
        }
        
        if (originalRoute != null) {
            Log.d("HomeFragment", "✅ Found matching Route: ${originalRoute.title}")
            navigateToTripDetails(originalRoute)
        } else {
            Log.e("HomeFragment", "❌ Could not find matching Route for: ${trip.name}")
            showToast("Unable to load trip details")
        }
    }
    
    private fun setupObservers() {
        // Observe loading state
        routeListViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // You can add a progress bar here if needed
            Log.d("HomeFragment", "Loading state: $isLoading")
        })
        
        // Observe user routes from ViewModel
        routeListViewModel.userRoutes.observe(viewLifecycleOwner, Observer { routes ->
            Log.i("HomeFragment", "✅ ROUTES RECEIVED: ${routes.size} routes from API")
            routes.forEachIndexed { index, route ->
                Log.d("HomeFragment", "Route $index: ${route.title} (${route.startDate} to ${route.endDate})")
            }
            userRoutes = routes
            updateTripsUI()
        })
        
        // Observe errors
        routeListViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("HomeFragment", "❌ ERROR RECEIVED: $it")
                val errorMessage = when {
                    it.contains("timeout", ignoreCase = true) || it.contains("network", ignoreCase = true) -> {
                        Log.w("HomeFragment", "Network/timeout error detected")
                        "Network connection slow. Please check your internet and try again."
                    }
                    it.contains("unauthorized", ignoreCase = true) -> {
                        Log.w("HomeFragment", "Authentication error detected")
                        "Session expired. Please log in again."
                    }
                    else -> {
                        Log.e("HomeFragment", "Unknown error type: $it")
                        "Failed to load trips: $it"
                    }
                }
                Log.i("HomeFragment", "Showing error message to user: $errorMessage")
                showToast(errorMessage)
                updateTripsUI() // Show empty state
            }
        })
        
        // Observe top-rated places
        placeViewModel.topRatedPlaces.observe(viewLifecycleOwner, Observer { places ->
            Log.i("HomeFragment", "✅ TOP RATED PLACES RECEIVED: ${places.size} places from API")
            topRatedPlaces = places // Store original data for navigation
            updateDestinationsUI(places)
        })
        
        // Observe loading state for destinations
        placeViewModel.isLoadingTopRated.observe(viewLifecycleOwner, Observer { isLoading ->
            Log.d("HomeFragment", "Top rated places loading state: $isLoading")
            // You can add a progress bar for destinations if needed
        })
        
        // Observe place errors
        placeViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("HomeFragment", "❌ DESTINATIONS ERROR: $it")
                showToast("Failed to load destinations: $it")
            }
        })
        
        // Load top-rated places
        placeViewModel.getTopRatedPlaces()
    }
    
    private fun fetchUserRoutes() {
        Log.d("HomeFragment", "Fetching user routes...")
        routeListViewModel.loadUserRoutes()
    }
    
    // Debug method - you can call this to test the API manually
    private fun debugRefreshTrips() {
        Log.d("HomeFragment", "=== DEBUG: Manual refresh triggered ===")
        userRoutes = emptyList()
        updateTripsUI() // Show empty state first
        fetchUserRoutes() // Then fetch new data
    }
    
    private fun updateTripsUI() {
        Log.d("HomeFragment", "🔄 UPDATE TRIPS UI: Starting with ${userRoutes.size} total routes")
        
        // Filter for upcoming trips only and limit to 4
        val upcomingTrips = getUpcomingTrips(userRoutes)
        Log.i("HomeFragment", "📅 UPCOMING TRIPS: ${upcomingTrips.size} trips after filtering")
        
        if (upcomingTrips.isEmpty()) {
            Log.i("HomeFragment", "📭 EMPTY STATE: No upcoming trips, showing empty state")
            // Show empty state
            binding.tripsRecyclerView.visibility = View.GONE
            binding.emptyTripsStateLayout.visibility = View.VISIBLE
        } else {
            Log.i("HomeFragment", "📋 TRIPS LIST: Showing ${upcomingTrips.size} upcoming trips")
            // Show trips list
            binding.tripsRecyclerView.visibility = View.VISIBLE
            binding.emptyTripsStateLayout.visibility = View.GONE
            
            // Convert Routes to Trips for the adapter
            val trips = upcomingTrips.map { route ->
                // Get the first available image from route activities (optimized)
                val imageUrl = getFirstImageFromRoute(route)
                
                Trip(
                    name = route.title,
                    imageUrl = imageUrl
                )
            }
            Log.i("HomeFragment", "✅ ADAPTER UPDATE: Updating adapter with ${trips.size} trips")
            tripsAdapter.updateTrips(trips)
        }
    }
    
    private fun getUpcomingTrips(routes: List<Route>): List<Route> {
        return try {
            val currentDate = java.time.LocalDate.now()
            Log.d("HomeFragment", "🗓️ FILTERING: Current date is $currentDate, filtering ${routes.size} routes")
            
            val upcomingRoutes = routes.filter { route ->
                try {
                    val startDate = java.time.LocalDate.parse(route.startDate)
                    val isUpcoming = startDate.isAfter(currentDate) || startDate.isEqual(currentDate)
                    Log.d("HomeFragment", "📅 Route '${route.title}': ${route.startDate} -> ${if (isUpcoming) "UPCOMING" else "PAST"}")
                    isUpcoming
                } catch (e: Exception) {
                    Log.e("HomeFragment", "❌ DATE PARSE ERROR for route '${route.title}': ${route.startDate}", e)
                    false // Exclude routes with invalid dates
                }
            }
            
            val sortedRoutes = upcomingRoutes.sortedBy { it.startDate }
            Log.d("HomeFragment", "📊 SORTED: ${sortedRoutes.size} upcoming routes sorted by date")
            
            val finalRoutes = sortedRoutes.take(4)
            Log.i("HomeFragment", "🎯 FINAL RESULT: Taking top ${finalRoutes.size} upcoming trips")
            
            finalRoutes
        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ FILTER ERROR: Exception during filtering", e)
            val fallback = routes.take(4)
            Log.w("HomeFragment", "🔄 FALLBACK: Returning first ${fallback.size} routes without filtering")
            fallback
        }
    }
    
    private fun getFirstImageFromRoute(route: Route): String {
        return try {
            Log.d("HomeFragment", "🖼️ EXTRACTING IMAGE for route: '${route.title}'")
            Log.d("HomeFragment", "   📅 Days count: ${route.days?.size ?: 0}")
            
            val allActivities = route.days
                .asSequence() // Use sequence for better performance
                .flatMap { it.activities.asSequence() }
                .toList()
            
            Log.d("HomeFragment", "   🎯 Total activities: ${allActivities.size}")
            
            val images = allActivities.mapNotNull { it.image }
            Log.d("HomeFragment", "   🖼️ Activities with images: ${images.size}")
            
            val firstImage = images.firstOrNull { it.isNotEmpty() }
            Log.d("HomeFragment", "   ✅ First image found: ${if (firstImage != null) "YES" else "NO"}")
            if (firstImage != null) {
                Log.d("HomeFragment", "   🖼️ Image URL: $firstImage")
            }
            
            firstImage ?: ""
        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ Error extracting image from route '${route.title}'", e)
            ""
        }
    }
    
    private fun updateDestinationsUI(topRatedPlaces: List<TopRatedPlace>) {
        Log.d("HomeFragment", "🏛️ UPDATING DESTINATIONS UI with ${topRatedPlaces.size} places")
        
        // Convert TopRatedPlace to Destination for the adapter
        val destinations = topRatedPlaces.map { place ->
            convertTopRatedPlaceToDestination(place)
        }
        
        Log.i("HomeFragment", "✅ DESTINATIONS UPDATE: Updating adapter with ${destinations.size} destinations")
        
        // Update the existing adapter with new data
        destinationsAdapter.updateDestinations(destinations)
    }
    
    private fun convertTopRatedPlaceToDestination(topRatedPlace: TopRatedPlace): Destination {
        val displayName = "${topRatedPlace.name}, ${topRatedPlace.city}"
        val imageUrl = topRatedPlace.image ?: ""
        val rating = topRatedPlace.wayfareRating?.toFloat() ?: topRatedPlace.rating?.toFloat() ?: 0.0f
        
        Log.d("HomeFragment", "🎯 Converting place: ${topRatedPlace.name} -> $displayName (rating: $rating)")
        Log.d("HomeFragment", "🖼️ RAW IMAGE URL from API: '$imageUrl'")
        
        // Check if URL contains problematic domains and log details
        if (imageUrl.contains("tripadvisor.com")) {
            Log.w("HomeFragment", "⚠️ TripAdvisor URL detected: $imageUrl")
            if (imageUrl.startsWith("http://")) {
                Log.e("HomeFragment", "❌ HTTP URL (should be HTTPS): $imageUrl")
            }
        }
        
        return Destination(
            name = displayName,
            imageUrl = imageUrl,
            rating = rating
        )
    }
    
    private fun navigateToAllDestinations() {
        Log.d("HomeFragment", "Navigating to All Destinations")
        val fragment = AllDestinationsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("AllDestinations")
            .commit()
    }

    private fun navigateToMyTrips() {
        Log.d("HomeFragment", "Navigating to My Trips")
        val fragment = MyTripsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("MyTrips")
            .commit()
    }
    
    private fun navigateToDestinationDetails(destination: TopRatedPlace) {
        Log.d("HomeFragment", "Navigating to Destination Details: ${destination.name}")
        val fragment = DestinationDetailsFragment.newInstance(destination)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("DestinationDetails")
            .commit()
    }
    
    private fun navigateToTripDetails(route: Route) {
        Log.d("HomeFragment", "Navigating to Trip Details: ${route.title}")
        val fragment = TripDetailsFragment.newInstance(route)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("TripDetails")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data classes for sample data
data class Destination(
    val name: String,
    val imageUrl: String,
    val rating: Float
)

data class Trip(
    val name: String,
    val imageUrl: String
)