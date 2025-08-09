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
import com.zeynekurtulus.wayfare.databinding.FragmentSearchBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.RouteSearchParams
import com.zeynekurtulus.wayfare.presentation.adapters.MyTripsAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import kotlinx.coroutines.*

/**
 * SearchFragment - Fragment for searching public routes
 * 
 * This fragment handles search functionality including:
 * - Text search by title, city, destination
 * - Filter options (budget, category, travel style)
 * - Search results with RecyclerView
 * - Navigation to route details
 */
class SearchFragment : Fragment() {
    
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var searchAdapter: MyTripsAdapter
    private var searchJob: Job? = null
    
    // Current search parameters
    private var currentQuery: String = ""
    private var currentBudget: String? = null
    private var currentCategory: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("SearchFragment", "🔍 onCreateView called - Creating SearchFragment UI")
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        Log.d("SearchFragment", "✅ SearchFragment layout inflated successfully")
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("SearchFragment", "✅ onViewCreated called - Setting up SearchFragment")
        setupRecyclerView()
        setupSearchInput()
        setupFilters()
        setupObservers()
        showEmptyState()
        Log.d("SearchFragment", "🎉 SearchFragment setup complete!")
    }
    
    private fun setupRecyclerView() {
        searchAdapter = MyTripsAdapter(
            isGridLayout = false,
            onTripClick = { route ->
                // Navigate to route details with feedback functionality
                navigateToRouteDetails(route)
            },
            onMenuClick = { route ->
                // For search results, we might not show menu or show limited options
                showToast("Public route from ${route.city}")
            }
        )
        
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }
    
    private fun setupSearchInput() {
        // Set up search text watcher with debouncing
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                currentQuery = query
                
                // Cancel previous search
                searchJob?.cancel()
                
                if (query.length >= 2) {
                    // Debounce search by 300ms
                    searchJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        performSearch()
                    }
                } else if (query.isEmpty()) {
                    showEmptyState()
                }
            }
        })
        
        // Handle search action on keyboard
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }
    
    private fun setupFilters() {
        // Budget filters
        binding.chipBudgetLow.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "low" else null
            if (isChecked) {
                binding.chipBudgetMedium.isChecked = false
                binding.chipBudgetHigh.isChecked = false
            }
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        binding.chipBudgetMedium.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "medium" else null
            if (isChecked) {
                binding.chipBudgetLow.isChecked = false
                binding.chipBudgetHigh.isChecked = false
            }
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        binding.chipBudgetHigh.setOnCheckedChangeListener { _, isChecked ->
            currentBudget = if (isChecked) "high" else null
            if (isChecked) {
                binding.chipBudgetLow.isChecked = false
                binding.chipBudgetMedium.isChecked = false
            }
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        // Category filters
        binding.chipCultural.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "cultural" else null
            clearOtherCategoryChips(binding.chipCultural)
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        binding.chipAdventure.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "adventure" else null
            clearOtherCategoryChips(binding.chipAdventure)
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        binding.chipBeach.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "beach" else null
            clearOtherCategoryChips(binding.chipBeach)
            if (currentQuery.isNotEmpty()) performSearch()
        }
        
        binding.chipNature.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "nature" else null
            clearOtherCategoryChips(binding.chipNature)
            if (currentQuery.isNotEmpty()) performSearch()
        }
    }
    
    private fun clearOtherCategoryChips(selectedChip: Chip) {
        val categoryChips = listOf(
            binding.chipCultural,
            binding.chipAdventure,
            binding.chipBeach,
            binding.chipNature
        )
        
        categoryChips.forEach { chip ->
            if (chip != selectedChip) {
                chip.isChecked = false
            }
        }
    }
    
    private fun setupObservers() {
        // Observe search results
        routeListViewModel.searchResults.observe(viewLifecycleOwner, Observer { routes ->
            if (routes.isNotEmpty()) {
                showSearchResults(routes)
                Log.d("SearchFragment", "Search successful: ${routes.size} routes found")
            } else {
                showNoResultsState()
                Log.d("SearchFragment", "Search returned no results")
            }
        })
        
        // Observe loading state
        routeListViewModel.isLoadingPublic.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingState()
                Log.d("SearchFragment", "Search loading...")
            }
        })
        
        // Observe errors
        routeListViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                showErrorState(it)
                Log.e("SearchFragment", "Search error: $it")
            }
        })
    }
    
    private fun performSearch() {
        if (currentQuery.length < 2) {
            showEmptyState()
            return
        }
        
        Log.d("SearchFragment", "Performing search - Query: '$currentQuery', Budget: $currentBudget, Category: $currentCategory")
        
        val searchParams = RouteSearchParams(
            q = currentQuery.takeIf { it.isNotEmpty() },
            budget = currentBudget,
            category = currentCategory,
            limit = 20,
            sortBy = "popularity"
        )
        
        routeListViewModel.searchPublicRoutes(searchParams)
    }
    

    
    private fun showEmptyState() {
        binding.apply {
            searchResultsRecyclerView.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            noResultsLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        }
    }
    
    private fun showLoadingState() {
        binding.apply {
            searchResultsRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            noResultsLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
        }
    }
    
    private fun showSearchResults(routes: List<Route>) {
        searchAdapter.updateTrips(routes)
        binding.apply {
            loadingLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            noResultsLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showNoResultsState() {
        binding.apply {
            searchResultsRecyclerView.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            errorLayout.visibility = View.GONE
            noResultsLayout.visibility = View.VISIBLE
        }
    }
    
    private fun showErrorState(message: String) {
        binding.apply {
            searchResultsRecyclerView.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
            noResultsLayout.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            errorMessageText.text = message
            
            retryButton.setOnClickListener {
                performSearch()
            }
        }
    }
    
    private fun navigateToRouteDetails(route: Route) {
        // TODO: Navigate to route details fragment with feedback functionality
        // This will be implemented in the next task
        showToast("Opening route details for: ${route.title}")
        Log.d("SearchFragment", "Navigating to route details: ${route.title}")
    }
    
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        Log.d("SearchFragment", "🔗 SearchFragment onAttach called")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("SearchFragment", "▶️ SearchFragment onResume called - Fragment is now visible")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("SearchFragment", "⏸️ SearchFragment onPause called")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("SearchFragment", "💀 SearchFragment onDestroyView called")
        searchJob?.cancel()
        _binding = null
    }
}