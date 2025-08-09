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
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentSearchPlacesBinding
import com.zeynekurtulus.wayfare.domain.model.Place
import com.zeynekurtulus.wayfare.domain.model.SearchPlaces
import com.zeynekurtulus.wayfare.presentation.adapters.PlaceSearchAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.PlaceViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import kotlinx.coroutines.*

class SearchPlacesFragment : Fragment() {
    
    private var _binding: FragmentSearchPlacesBinding? = null
    private val binding get() = _binding!!
    
    private val placeViewModel: PlaceViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }
    
    private lateinit var searchAdapter: PlaceSearchAdapter
    
    // Search parameters
    private var currentQuery: String = ""
    private var currentCity: String = ""
    private var currentCategory: String? = null
    
    // Debounce search
    private var searchJob: Job? = null
    private val searchDelay = 300L
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchPlacesBinding.inflate(inflater, container, false)
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
        searchAdapter = PlaceSearchAdapter { place ->
            navigateToPlaceDetails(place)
        }
        
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }
    
    private fun setupSearchInput() {
        // Search query input
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
        
        // City input
        binding.cityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentCity = s?.toString()?.trim() ?: ""
                performSearch()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupFilters() {
        // Category filters
        binding.museumsChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Museums" else null
            updateFilterChips(binding.museumsChip)
            performSearch()
        }
        
        binding.themeParksChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Theme Parks" else null
            updateFilterChips(binding.themeParksChip)
            performSearch()
        }
        
        binding.restaurantsChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Restaurants" else null
            updateFilterChips(binding.restaurantsChip)
            performSearch()
        }
        
        binding.nightlifeChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Nightlife" else null
            updateFilterChips(binding.nightlifeChip)
            performSearch()
        }
        
        binding.outdoorChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Outdoor Activities" else null
            updateFilterChips(binding.outdoorChip)
            performSearch()
        }
        
        binding.shoppingChip.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Shopping" else null
            updateFilterChips(binding.shoppingChip)
            performSearch()
        }
    }
    
    private fun updateFilterChips(selectedChip: com.google.android.material.chip.Chip) {
        // Uncheck all other category chips (single selection)
        val categoryChips = listOf(
            binding.museumsChip, binding.themeParksChip, binding.restaurantsChip,
            binding.nightlifeChip, binding.outdoorChip, binding.shoppingChip
        )
        
        categoryChips.forEach { chip ->
            if (chip != selectedChip) {
                chip.isChecked = false
            }
        }
    }
    
    private fun setupObservers() {
        placeViewModel.places.observe(viewLifecycleOwner, Observer { places ->
            Log.d("SearchPlacesFragment", "Received ${places.size} place search results")
            showSearchResults(places)
        })
        
        placeViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        })
        
        placeViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                showErrorState(it)
                Log.e("SearchPlacesFragment", "Place search error: $it")
            }
        })
        
        binding.retryButton.setOnClickListener {
            performSearch()
        }
    }
    
    private fun performSearch() {
        searchJob?.cancel()
        
        // City is required for place search
        if (currentCity.isBlank()) {
            showEmptyState()
            return
        }
        
        if (currentCity.length < 2) {
            return // Don't search for very short city names
        }
        
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(searchDelay)
            
            Log.d("SearchPlacesFragment", "Performing place search: query='$currentQuery', city='$currentCity', category=$currentCategory")
            
            // For the new API structure, if city is empty but query has value, treat query as the city
            val searchCity = if (currentCity.isBlank() && currentQuery.isNotBlank()) {
                currentQuery  // Use search query as city when city field is empty
            } else if (currentCity.isNotBlank()) {
                currentCity   // Use city field when available
            } else {
                "Rome"        // Default fallback
            }
            
            // Use query only for place name/keywords search, not as city
            val searchQuery = if (currentCity.isNotBlank() && currentQuery.isNotBlank()) {
                currentQuery  // Use query for place search when we have a separate city
            } else {
                ""           // No place-specific search
            }
            
            Log.d("SearchPlacesFragment", "Final search parameters - searchQuery: '$searchQuery', searchCity: '$searchCity', category: $currentCategory")
            
            placeViewModel.searchPlaces(
                query = searchQuery,
                city = searchCity,
                category = currentCategory
            )
        }
    }
    
    private fun navigateToPlaceDetails(place: Place) {
        Log.d("SearchPlacesFragment", "Navigating to place details: ${place.name}")
        val fragment = PlaceDetailsFragment.newInstance(place)
        
        // Access the activity's fragment manager since we're inside a ViewPager2
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("PlaceDetails")
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
    
    private fun showSearchResults(places: List<Place>) {
        if (places.isEmpty()) {
            showNoResultsState()
        } else {
            searchAdapter.updatePlaces(places)
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
        Log.d("SearchPlacesFragment", "Resetting search results")
        currentQuery = ""
        currentCity = ""
        currentCategory = null
        
        binding.apply {
            // Clear search inputs
            searchEditText.setText("")
            cityEditText.setText("")
            
            // Reset category chips
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