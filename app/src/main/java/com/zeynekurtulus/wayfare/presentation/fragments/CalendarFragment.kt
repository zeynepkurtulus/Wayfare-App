package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentCalendarBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.presentation.adapters.CalendarTripsAdapter
import com.zeynekurtulus.wayfare.presentation.calendar.CustomCalendarView
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import com.zeynekurtulus.wayfare.utils.BeautifulDialogUtils
import com.zeynekurtulus.wayfare.presentation.fragments.OfflineDownloadsFragment
import com.zeynekurtulus.wayfare.presentation.activities.MainActivity
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.OfflineRouteViewModel
import com.zeynekurtulus.wayfare.presentation.viewmodels.DownloadProgress
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * CalendarFragment - Calendar and trip planning screen
 */
class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel for fetching user routes
    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    // ViewModel for offline route management
    private val offlineRouteViewModel: OfflineRouteViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var calendarTripsAdapter: CalendarTripsAdapter
    private lateinit var customCalendarView: CustomCalendarView
    private var userRoutes = listOf<Route>()
    private var filteredRoutes = listOf<Route>()
    private var selectedDate: LocalDate? = null
    private var currentFilter = TripFilter.ALL
    
    enum class TripFilter {
        ALL, UPCOMING, ONGOING, PAST
    }
    
    // Date formatters
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCalendarUI()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        updateMonthDisplay()
        
        // Initial data load
        android.util.Log.d("CalendarFragment", "🔄 Initial data load for calendar")
        refreshCalendarData()
    }
    
    private fun setupCalendarUI() {
        // Set current month display
        val currentDate = LocalDate.now()
        binding.currentMonthTextView.text = currentDate.format(monthYearFormatter)
        
        // Initialize custom calendar view by accessing views through root
        val calendarGridContainer = binding.root.findViewById<LinearLayout>(R.id.calendarGridContainer)
        val monthYearTextView = binding.root.findViewById<TextView>(R.id.monthYearTextView)
        val previousMonthButton = binding.root.findViewById<View>(R.id.previousMonthButton)
        val nextMonthButton = binding.root.findViewById<View>(R.id.nextMonthButton)
        
        customCalendarView = CustomCalendarView(
            context = requireContext(),
            calendarGridContainer = calendarGridContainer,
            monthYearTextView = monthYearTextView,
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate)
            }
        )
        
        // Setup month navigation
        previousMonthButton.setOnClickListener {
            customCalendarView.goToPreviousMonth()
        }
        
        nextMonthButton.setOnClickListener {
            customCalendarView.goToNextMonth()
        }
    }
    
    private fun setupRecyclerView() {
        calendarTripsAdapter = CalendarTripsAdapter(
            trips = emptyList(),
            onTripClick = { trip -> onTripClicked(trip) },
            onDownloadClick = { trip -> onTripDownloadClicked(trip) },
            isRouteDownloaded = { routeId -> offlineRouteViewModel.isRouteDownloaded(routeId) }
        )
        
        binding.tripEventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calendarTripsAdapter
            isNestedScrollingEnabled = false
        }
    }
    
    private fun setupClickListeners() {
        // Refresh button
        binding.calendarRefreshButton.setOnClickListener {
            refreshCalendarData()
        }
        
        // Empty state button
        binding.createTripButton.setOnClickListener {
            navigateToTripMaker()
        }
        
        // Close date details when clicking outside
        binding.selectedDateDetailsCard.setOnClickListener {
            hideSelectedDateDetails()
        }
        
        // Filter chips
        binding.chipAllTrips.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = TripFilter.ALL
                updateFilterSelection()
                applyFilter()
            }
        }
        
        binding.chipUpcomingTrips.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = TripFilter.UPCOMING
                updateFilterSelection()
                applyFilter()
            }
        }
        
        binding.chipOngoingTrips.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = TripFilter.ONGOING
                updateFilterSelection()
                applyFilter()
            }
        }
        
        binding.chipPastTrips.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = TripFilter.PAST
                updateFilterSelection()
                applyFilter()
            }
        }
    }
    
    private fun setupObservers() {
        // Observe user routes
        routeListViewModel.userRoutes.observe(viewLifecycleOwner, Observer { routes ->
            android.util.Log.d("CalendarFragment", "📅 Received ${routes.size} routes for calendar")
            userRoutes = routes
            updateCalendarDisplay()
            // Update calendar highlighting with trip dates
            if (::customCalendarView.isInitialized) {
                customCalendarView.setTripDates(routes)
            }
        })
        
        // Observe loading state
        routeListViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // You can add a progress indicator here if needed
            android.util.Log.d("CalendarFragment", "Calendar loading state: $isLoading")
        })
        
        // Observe errors
        routeListViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                android.util.Log.e("CalendarFragment", "Calendar error: $it")
                showToast("Failed to load trips: $it")
            }
        })
        
        // Observe download progress using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            offlineRouteViewModel.downloadProgress.collect { progressMap ->
                progressMap.forEach { (routeId: String, progress: DownloadProgress) ->
                    when (progress) {
                        is DownloadProgress.Completed -> {
                            val routeName = userRoutes.find { it.routeId == routeId }?.title ?: "Trip"
                            BeautifulDialogUtils.showDownloadSuccessDialog(
                                context = requireContext(),
                                tripName = routeName,
                                onViewOfflineDownloads = { navigateToOfflineDownloads() }
                            )
                            android.util.Log.d("CalendarFragment", "✅ Download completed: $routeName")
                            // Update the adapter to reflect new download status
                            calendarTripsAdapter.notifyDataSetChanged()
                        }
                        is DownloadProgress.Failed -> {
                            val routeName = userRoutes.find { it.routeId == routeId }?.title ?: "Trip"
                            BeautifulDialogUtils.showDownloadFailureDialog(
                                context = requireContext(),
                                tripName = routeName,
                                errorMessage = progress.error,
                                onRetry = { 
                                    // Find the route and retry download
                                    userRoutes.find { it.routeId == routeId }?.let { route ->
                                        offlineRouteViewModel.downloadRoute(route.routeId)
                                    }
                                }
                            )
                            android.util.Log.e("CalendarFragment", "❌ Download failed: $routeName - ${progress.error}")
                        }
                        else -> { /* Handle other states if needed */ }
                    }
                }
            }
        }
        
        // Observe downloaded routes changes to refresh adapter
        offlineRouteViewModel.downloadedRoutes.observe(viewLifecycleOwner) { downloadedRoutes ->
            // Refresh adapter to update download status indicators
            calendarTripsAdapter.notifyDataSetChanged()
            android.util.Log.d("CalendarFragment", "🔄 Downloaded routes updated, refreshing adapter")
        }
    }
    
    private fun refreshCalendarData() {
        android.util.Log.d("CalendarFragment", "🔄 Refreshing calendar data...")
        routeListViewModel.loadUserRoutes()
    }
    
    private fun updateCalendarDisplay() {
        // Apply current filter and sort by start date
        applyFilter()
        
        android.util.Log.d("CalendarFragment", "✅ Calendar display updated with ${filteredRoutes.size} trips")
    }
    
    private fun applyFilter() {
        val currentDate = LocalDate.now()
        
        filteredRoutes = when (currentFilter) {
            TripFilter.ALL -> userRoutes
            TripFilter.UPCOMING -> userRoutes.filter { route ->
                try {
                    val startDate = LocalDate.parse(route.startDate)
                    startDate.isAfter(currentDate)
                } catch (e: Exception) { false }
            }
            TripFilter.ONGOING -> userRoutes.filter { route ->
                try {
                    val startDate = LocalDate.parse(route.startDate)
                    val endDate = LocalDate.parse(route.endDate)
                    !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate)
                } catch (e: Exception) { false }
            }
            TripFilter.PAST -> userRoutes.filter { route ->
                try {
                    val endDate = LocalDate.parse(route.endDate)
                    endDate.isBefore(currentDate)
                } catch (e: Exception) { false }
            }
        }.sortedBy { it.startDate }
        
        // Update UI
        updateTripsDisplay()
        updateHeaderText()
    }
    
    private fun updateTripsDisplay() {
        // Update trip count
        binding.tripCountTextView.text = "${filteredRoutes.size} trip${if (filteredRoutes.size != 1) "s" else ""}"
        
        // Update RecyclerView
        calendarTripsAdapter.updateTrips(filteredRoutes)
        
        // Show/hide empty state
        if (filteredRoutes.isEmpty()) {
            binding.tripEventsRecyclerView.visibility = View.GONE
            binding.emptyCalendarStateLayout.visibility = View.VISIBLE
        } else {
            binding.tripEventsRecyclerView.visibility = View.VISIBLE
            binding.emptyCalendarStateLayout.visibility = View.GONE
        }
    }
    
    private fun updateHeaderText() {
        val headerText = when (currentFilter) {
            TripFilter.ALL -> "📅 All Trips"
            TripFilter.UPCOMING -> "🔮 Upcoming Trips"
            TripFilter.ONGOING -> "✈️ Ongoing Trips"
            TripFilter.PAST -> "📝 Past Trips"
        }
        binding.tripsHeaderTextView.text = headerText
    }
    
    private fun updateFilterSelection() {
        // Clear all selections first
        binding.chipAllTrips.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.grey_300)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        binding.chipUpcomingTrips.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.grey_300)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        binding.chipOngoingTrips.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.grey_300)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        binding.chipPastTrips.apply {
            isChecked = false
            setChipBackgroundColorResource(R.color.grey_300)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
        
        // Set the selected one
        val selectedChip = when (currentFilter) {
            TripFilter.ALL -> binding.chipAllTrips
            TripFilter.UPCOMING -> binding.chipUpcomingTrips
            TripFilter.ONGOING -> binding.chipOngoingTrips
            TripFilter.PAST -> binding.chipPastTrips
        }
        
        selectedChip.apply {
            isChecked = true
            setChipBackgroundColorResource(R.color.primary_blue_700)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
    
    private fun updateMonthDisplay() {
        val currentDate = LocalDate.now()
        binding.currentMonthTextView.text = currentDate.format(monthYearFormatter)
    }
    
    private fun onDateSelected(date: LocalDate) {
        selectedDate = date
        android.util.Log.d("CalendarFragment", "📅 Date selected: $date")
        
        // Find trips for this date
        val tripsOnDate = getTripsForDate(date)
        
        if (tripsOnDate.isNotEmpty()) {
            showSelectedDateDetails(date, tripsOnDate)
        } else {
            hideSelectedDateDetails()
            showToast("No trips on ${date.format(displayDateFormatter)}")
        }
    }
    
    private fun getTripsForDate(date: LocalDate): List<Route> {
        return userRoutes.filter { route ->
            try {
                val startDate = LocalDate.parse(route.startDate)
                val endDate = LocalDate.parse(route.endDate)
                !date.isBefore(startDate) && !date.isAfter(endDate)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun showSelectedDateDetails(date: LocalDate, trips: List<Route>) {
        binding.selectedDateTitleTextView.text = date.format(displayDateFormatter)
        binding.selectedDateTripsTextView.text = "${trips.size} trip${if (trips.size != 1) "s" else ""} on this date"
        
        // Clear existing trip details
        binding.selectedDateTripsContainer.removeAllViews()
        
        // Add trip details dynamically
        trips.forEach { trip ->
            val tripView = layoutInflater.inflate(R.layout.item_calendar_trip, binding.selectedDateTripsContainer, false)
            
            // Populate trip details
            val tripTitleTextView = tripView.findViewById<TextView>(R.id.tripTitleTextView)
            val tripLocationTextView = tripView.findViewById<TextView>(R.id.tripLocationTextView)
            val tripDatesTextView = tripView.findViewById<TextView>(R.id.tripDatesTextView)
            val tripDurationTextView = tripView.findViewById<TextView>(R.id.tripDurationTextView)
            val tripImageView = tripView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.tripImageView)
            
            tripTitleTextView.text = trip.title
            tripLocationTextView.text = "${trip.city}, ${trip.country}"
            tripDatesTextView.text = "${trip.startDate} - ${trip.endDate}"
            
            // Calculate duration
            val duration = try {
                val start = java.time.LocalDate.parse(trip.startDate)
                val end = java.time.LocalDate.parse(trip.endDate)
                java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1
            } catch (e: Exception) {
                1
            }
            tripDurationTextView.text = "$duration days"
            
            // Load trip image if available (use first place image or placeholder)
            val firstPlaceImage = trip.mustVisit.firstOrNull()?.image
            if (!firstPlaceImage.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(firstPlaceImage)
                    .placeholder(R.drawable.ic_map_placeholder)
                    .error(R.drawable.ic_map_placeholder)
                    .into(tripImageView)
            } else {
                tripImageView.setImageResource(R.drawable.ic_map_placeholder)
            }
            
            // Make trip view clickable
            tripView.setOnClickListener {
                onTripClicked(trip)
            }
            
            binding.selectedDateTripsContainer.addView(tripView)
        }
        
        binding.selectedDateDetailsCard.visibility = View.VISIBLE
        android.util.Log.d("CalendarFragment", "📋 Showing details for ${trips.size} trips on $date")
    }
    
    private fun hideSelectedDateDetails() {
        binding.selectedDateDetailsCard.visibility = View.GONE
    }
    
    private fun onTripClicked(trip: Route) {
        android.util.Log.d("CalendarFragment", "🎯 Trip clicked: ${trip.title}")
        
        // Navigate to trip details fragment
        val fragment = TripDetailsFragment.newInstance(trip)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("TripDetails")
            .commit()
    }
    
    private fun navigateToTripMaker() {
        (activity as? androidx.appcompat.app.AppCompatActivity)?.let { activity ->
            if (activity is com.zeynekurtulus.wayfare.presentation.activities.MainActivity) {
                activity.switchToTripMaker()
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun onTripDownloadClicked(trip: Route) {
        val isDownloaded = offlineRouteViewModel.isRouteDownloaded(trip.routeId)
        
        if (isDownloaded) {
            // Route is already downloaded, show success message
            BeautifulDialogUtils.showDownloadSuccessDialog(
                context = requireContext(),
                tripName = trip.title,
                onViewOfflineDownloads = { navigateToOfflineDownloads() }
            )
        } else {
            // Download the route
            offlineRouteViewModel.downloadRoute(trip.routeId)
            android.util.Log.d("CalendarFragment", "🔽 Downloading trip: ${trip.title} (${trip.routeId})")
        }
    }
    
    private fun navigateToOfflineDownloads() {
        // First switch to Profile tab in bottom navigation
        (activity as? MainActivity)?.switchToProfile()
        
        // Then navigate to offline downloads fragment
        val fragment = OfflineDownloadsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("OfflineDownloads")
            .commit()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh calendar data when returning to this screen
        refreshCalendarData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}