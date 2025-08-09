package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentMyTripsBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.presentation.adapters.MyTripsAdapter
import com.zeynekurtulus.wayfare.presentation.navigation.BottomNavigationHandler
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * MyTripsFragment - Comprehensive trips listing screen
 * 
 * Features:
 * - List/Grid layout toggle
 * - Status filtering (All, Upcoming, Ongoing, Completed)
 * - Sorting options (Date, Name, Duration)
 * - Pull-to-refresh
 * - Loading/empty/error states
 */
class MyTripsFragment : Fragment() {

    private var _binding: FragmentMyTripsBinding? = null
    private val binding get() = _binding!!

    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }

    private lateinit var tripsAdapter: MyTripsAdapter
    private var allTrips: List<Route> = emptyList()
    private var filteredTrips: List<Route> = emptyList()
    
    // UI State
    private var isGridLayout = false
    private var currentFilter = TripFilter.ALL
    private var currentSort = TripSort.DATE
    private var showSortOptions = false

    enum class TripFilter { ALL, UPCOMING, ONGOING, COMPLETED }
    enum class TripSort { DATE, NAME, DURATION }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        loadTrips()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupSorting()
        setupSwipeRefresh()
        setupEmptyState()
        setupRetryButton()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            Log.d("MyTripsFragment", "Back button clicked")
            parentFragmentManager.popBackStack()
        }

        binding.layoutToggleButton.setOnClickListener {
            toggleLayout()
        }

        binding.filterButton.setOnClickListener {
            toggleSortOptions()
        }
    }

    private fun setupRecyclerView() {
        tripsAdapter = MyTripsAdapter(
            isGridLayout = isGridLayout,
            onTripClick = { trip -> onTripClicked(trip) },
            onMenuClick = { trip -> onTripMenuClicked(trip) }
        )

        updateRecyclerViewLayout()
        binding.tripsRecyclerView.adapter = tripsAdapter
    }

    private fun setupFilters() {
        val filters = listOf(
            R.id.chipAllTrips to TripFilter.ALL,
            R.id.chipUpcoming to TripFilter.UPCOMING,
            R.id.chipOngoing to TripFilter.ONGOING,
            R.id.chipCompleted to TripFilter.COMPLETED
        )
        
        filters.forEach { (chipId, filter) ->
            val chip = binding.filterChipGroup.findViewById<Chip>(chipId)
            chip?.setOnClickListener {
                if (chip.isChecked) {
                    currentFilter = filter
                    applyFiltersAndSort()
                    Log.d("MyTripsFragment", "Filter applied: $filter")
                }
            }
        }
    }

    private fun setupSorting() {
        val sortOptions = listOf(
            R.id.chipSortDate to TripSort.DATE,
            R.id.chipSortName to TripSort.NAME,
            R.id.chipSortDuration to TripSort.DURATION
        )
        
        sortOptions.forEach { (chipId, sort) ->
            val chip = binding.sortChipGroup.findViewById<Chip>(chipId)
            chip?.setOnClickListener {
                if (chip.isChecked) {
                    currentSort = sort
                    applyFiltersAndSort()
                    Log.d("MyTripsFragment", "Sort applied: $sort")
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadTrips()
        }
    }

    private fun setupEmptyState() {
        binding.createTripButton.setOnClickListener {
            navigateToTripMaker()
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            loadTrips()
        }
    }

    private fun setupObservers() {
        routeListViewModel.userRoutes.observe(viewLifecycleOwner, Observer { routes ->
            binding.swipeRefreshLayout.isRefreshing = false
            
            allTrips = routes
            applyFiltersAndSort()
            if (allTrips.isEmpty()) {
                showEmptyState()
            } else {
                showContentState()
            }
            Log.d("MyTripsFragment", "Loaded ${allTrips.size} trips")
        })
        
        // Observe loading state
        routeListViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading && !binding.swipeRefreshLayout.isRefreshing) {
                showLoadingState()
            }
        })
        
        // Observe errors
        routeListViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                showErrorState(it)
                Log.e("MyTripsFragment", "Error loading trips: $it")
            }
        })
        
        // Observe delete state
        routeListViewModel.deleteState.observe(viewLifecycleOwner, Observer { state ->
            handleDeleteState(state)
        })
    }

    private fun loadTrips() {
        Log.d("MyTripsFragment", "Loading user trips...")
        routeListViewModel.loadUserRoutes()
    }

    private fun applyFiltersAndSort() {
        // Apply filter
        filteredTrips = when (currentFilter) {
            TripFilter.ALL -> allTrips
            TripFilter.UPCOMING -> allTrips.filter { isUpcoming(it) }
            TripFilter.ONGOING -> allTrips.filter { isOngoing(it) }
            TripFilter.COMPLETED -> allTrips.filter { isCompleted(it) }
        }

        // Apply sort
        filteredTrips = when (currentSort) {
            TripSort.DATE -> filteredTrips.sortedBy { it.startDate }
            TripSort.NAME -> filteredTrips.sortedBy { it.title }
            TripSort.DURATION -> filteredTrips.sortedBy { calculateDuration(it) }
        }

        tripsAdapter.updateTrips(filteredTrips)
        updateTripsCount()
        
        if (filteredTrips.isEmpty() && allTrips.isNotEmpty()) {
            showEmptyState()
        } else if (filteredTrips.isNotEmpty()) {
            showContentState()
        }

        Log.d("MyTripsFragment", 
            "Filtered: ${filteredTrips.size}/${allTrips.size} trips " +
            "(filter: $currentFilter, sort: $currentSort)")
    }

    private fun toggleLayout() {
        isGridLayout = !isGridLayout
        
        // Update adapter layout mode
        tripsAdapter.updateLayoutMode(isGridLayout)
        
        // Update RecyclerView layout manager
        updateRecyclerViewLayout()
        
        // Update button icon
        val iconRes = if (isGridLayout) R.drawable.ic_list_view else R.drawable.ic_grid_view
        binding.layoutToggleButton.setImageResource(iconRes)
        
        Log.d("MyTripsFragment", "Layout toggled to: ${if (isGridLayout) "Grid" else "List"}")
    }

    private fun updateRecyclerViewLayout() {
        binding.tripsRecyclerView.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun toggleSortOptions() {
        showSortOptions = !showSortOptions
        binding.sortScrollView.visibility = if (showSortOptions) View.VISIBLE else View.GONE
        
        // Update filter button icon or color to indicate state
        val tint = if (showSortOptions) {
            requireContext().getColor(R.color.primary)
        } else {
            requireContext().getColor(R.color.text_primary)
        }
        binding.filterButton.setColorFilter(tint)
    }

    private fun updateTripsCount() {
        val countText = when {
            filteredTrips.isEmpty() -> "No trips found"
            filteredTrips.size == 1 -> "1 trip found"
            else -> "${filteredTrips.size} trips found"
        }
        binding.tripsCountText.text = countText
    }

    private fun onTripClicked(trip: Route) {
        Log.d("MyTripsFragment", "Trip clicked: ${trip.title}")
        
        // Navigate to trip details
        val fragment = TripDetailsFragment.newInstance(trip)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("TripDetails")
            .commit()
    }

    private fun onTripMenuClicked(trip: Route) {
        Log.d("MyTripsFragment", "Trip menu clicked: ${trip.title}")
        showTripOptionsMenu(trip)
    }
    
    private fun showTripOptionsMenu(trip: Route) {
        val popupMenu = android.widget.PopupMenu(requireContext(), view?.findViewById(R.id.menuButton))
        popupMenu.menuInflater.inflate(R.menu.trip_options_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_trip -> {
                    showDeleteConfirmationDialog(trip)
                    true
                }
                R.id.action_share_trip -> {
                    showToast("Share functionality coming soon!")
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    private fun showDeleteConfirmationDialog(trip: Route) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete \"${trip.title}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrip(trip)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteTrip(trip: Route) {
        trip.routeId?.let { routeId ->
            routeListViewModel.deleteRoute(routeId)
        } ?: run {
            showToast("Cannot delete trip: Invalid trip ID")
        }
    }
    
    private fun handleDeleteState(state: com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState) {
        when (state) {
            is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Loading -> {
                // Show loading indicator if needed
                Log.d("MyTripsFragment", "Deleting trip...")
            }
            is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Success -> {
                showToast("Trip deleted successfully")
                // Refresh the trips list
                loadTrips()
                Log.d("MyTripsFragment", "Trip deleted successfully: ${state.routeId}")
            }
            is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Error -> {
                showToast("Failed to delete trip: ${state.message}")
                Log.e("MyTripsFragment", "Failed to delete trip: ${state.message}")
            }
            is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Idle -> {
                // Do nothing
            }
        }
    }

    private fun navigateToTripMaker() {
        Log.d("MyTripsFragment", "Navigating to Trip Maker")
        
        // Navigate to Trip Maker tab by switching the bottom navigation
        val activity = requireActivity()
        if (activity is com.zeynekurtulus.wayfare.presentation.activities.MainActivity) {
            activity.switchToTripMaker()
        }
    }

    // Trip status helper methods
    private fun isUpcoming(trip: Route): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(trip.startDate)
            val today = Calendar.getInstance().time
            startDate?.after(today) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isOngoing(trip: Route): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(trip.startDate)
            val endDate = sdf.parse(trip.endDate)
            val today = Calendar.getInstance().time
            
            startDate?.let { start ->
                endDate?.let { end ->
                    today.after(start) && today.before(end)
                } ?: false
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun isCompleted(trip: Route): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDate = sdf.parse(trip.endDate)
            val today = Calendar.getInstance().time
            endDate?.before(today) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateDuration(trip: Route): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(trip.startDate)
            val endDate = sdf.parse(trip.endDate)
            
            if (startDate != null && endDate != null) {
                val diffInMillis = endDate.time - startDate.time
                (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1 // +1 to include both start and end day
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    // State management methods
    private fun showLoadingState() {
        binding.tripsRecyclerView.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
    }

    private fun showContentState() {
        binding.loadingLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.tripsRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.tripsRecyclerView.visibility = View.GONE
        binding.loadingLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.VISIBLE
    }

    private fun showErrorState(message: String) {
        binding.tripsRecyclerView.visibility = View.GONE
        binding.loadingLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorMessageText.text = message
    }

    override fun onResume() {
        super.onResume()
        // Refresh trips when returning to this screen
        loadTrips()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}