package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentSearchRoutesBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.RouteSearchParams
import com.zeynekurtulus.wayfare.presentation.adapters.MyTripsAdapter
import com.zeynekurtulus.wayfare.presentation.fragments.TripDetailsFragment
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import kotlinx.coroutines.*

class SearchRoutesFragment : Fragment() {
    
    private var _binding: FragmentSearchRoutesBinding? = null
    private val binding get() = _binding!!
    
    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var searchAdapter: MyTripsAdapter
    
    // Search parameters
    private var currentQuery: String = ""
    private var currentBudget: String? = null
    private var currentCategory: String? = null
    
    // Debounce search
    private var searchJob: Job? = null
    private val searchDelay = 300L
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchInput()
        setupFilters()
        setupObservers()
        showEmptyState()
    }
    
    private fun setupRecyclerView() {
        searchAdapter = MyTripsAdapter(
            isGridLayout = false,
            onTripClick = { route ->
                navigateToRouteDetails(route)
            },
            onMenuClick = { route ->
                // No menu for search results
            }
        )
        
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }
    
    private fun setupSearchInput() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                performSearch()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentQuery = binding.searchEditText.text?.toString()?.trim() ?: ""
                performSearch()
                true
            } else {
                false
            }
        }
    }
    
    private fun setupFilters() {
        // Budget filters
        binding.budgetLowChip.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "low" else null
            performSearch()
        }
        
        binding.budgetMediumChip.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "medium" else null
            performSearch()
        }
        
        binding.budgetHighChip.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "high" else null
            performSearch()
        }
        
        // Category filters
        binding.culturalChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "cultural" else null
            performSearch()
        }
        
        binding.adventureChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "adventure" else null
            performSearch()
        }
        
        binding.beachChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "beach" else null
            performSearch()
        }
        
        binding.natureChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "nature" else null
            performSearch()
        }
    }
    
    private fun setupObservers() {
        routeListViewModel.searchResults.observe(viewLifecycleOwner, Observer { routes ->
            Log.d("SearchRoutesFragment", "Received ${routes.size} search results")
            showSearchResults(routes)
        })
        
        routeListViewModel.isLoadingPublic.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        })
        
        routeListViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                showErrorState(it)
                Log.e("SearchRoutesFragment", "Search error: $it")
            }
        })
        
        binding.retryButton.setOnClickListener {
            performSearch()
        }
    }
    
    private fun performSearch() {
        searchJob?.cancel()
        
        if (currentQuery.isBlank()) {
            showEmptyState()
            return
        }
        
        if (currentQuery.length < 2) {
            return // Don't search for less than 2 characters
        }
        
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(searchDelay)
            
            val searchParams = RouteSearchParams(
                q = currentQuery,
                budget = currentBudget,
                category = currentCategory
            )
            
            Log.d("SearchRoutesFragment", "Performing search: query='$currentQuery', budget=$currentBudget, category=$currentCategory")
            routeListViewModel.searchPublicRoutes(searchParams)
        }
    }
    
    private fun navigateToRouteDetails(route: Route) {
        Log.d("SearchRoutesFragment", "Navigating to route details: ${route.title}")
        val fragment = TripDetailsFragment.newInstance(route)
        
        // Access the activity's fragment manager since we're inside a ViewPager2
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("RouteDetails")
            .commit()
    }
    
    private fun showEmptyState() {
        binding.apply {
            emptyStateLayout.visibility = View.VISIBLE
            searchRecyclerView.visibility = View.GONE
            loadingStateLayout.visibility = View.GONE
            noResultsStateLayout.visibility = View.GONE
            errorStateLayout.visibility = View.GONE
        }
    }
    
    private fun showLoadingState() {
        binding.apply {
            loadingStateLayout.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            noResultsStateLayout.visibility = View.GONE
            errorStateLayout.visibility = View.GONE
        }
    }
    
    private fun showSearchResults(routes: List<Route>) {
        if (routes.isEmpty()) {
            showNoResultsState()
        } else {
            searchAdapter.updateTrips(routes)
            binding.apply {
                searchRecyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
                loadingStateLayout.visibility = View.GONE
                noResultsStateLayout.visibility = View.GONE
                errorStateLayout.visibility = View.GONE
            }
        }
    }
    
    private fun showNoResultsState() {
        binding.apply {
            noResultsStateLayout.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            loadingStateLayout.visibility = View.GONE
            errorStateLayout.visibility = View.GONE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            errorStateLayout.visibility = View.VISIBLE
            errorMessageText.text = message
            emptyStateLayout.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            loadingStateLayout.visibility = View.GONE
            noResultsStateLayout.visibility = View.GONE
        }
    }
    
    fun resetSearchResults() {
        Log.d("SearchRoutesFragment", "Resetting search results")
        currentQuery = ""
        currentBudget = null
        currentCategory = null
        
        binding.apply {
            // Clear search input
            searchEditText.setText("")
            
            // Reset all filter chips
            filterChipGroup.clearCheck()
            
            // Show empty state
            showEmptyState()
        }
        
        // Cancel any pending search
        searchJob?.cancel()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}