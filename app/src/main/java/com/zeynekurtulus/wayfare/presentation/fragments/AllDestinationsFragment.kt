package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentAllDestinationsBinding
import com.zeynekurtulus.wayfare.domain.model.TopRatedPlace
import com.zeynekurtulus.wayfare.presentation.adapters.AllDestinationsAdapter
import com.zeynekurtulus.wayfare.presentation.viewmodels.PlaceViewModel
import com.zeynekurtulus.wayfare.utils.ApiResult
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast

/**
 * AllDestinationsFragment - Comprehensive destinations listing screen
 * 
 * Features:
 * - Grid layout for destinations
 * - Search functionality
 * - Category filtering
 * - Pull-to-refresh
 * - Loading/empty/error states
 */
class AllDestinationsFragment : Fragment() {

    private var _binding: FragmentAllDestinationsBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }

    private lateinit var destinationsAdapter: AllDestinationsAdapter
    private var allDestinations: List<TopRatedPlace> = emptyList()
    private var filteredDestinations: List<TopRatedPlace> = emptyList()
    private var currentCategory: String = "All"
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllDestinationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        loadDestinations()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupCategoryFilters()
        setupRetryButton()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            Log.d("AllDestinationsFragment", "Back button clicked")
            parentFragmentManager.popBackStack()
        }

        binding.searchButton.setOnClickListener {
            Log.d("AllDestinationsFragment", "Search button clicked")
            toggleSearchVisibility()
        }
    }

    private fun setupRecyclerView() {
        destinationsAdapter = AllDestinationsAdapter { destination ->
            onDestinationClicked(destination)
        }

        binding.destinationsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = destinationsAdapter
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                filterDestinations()
            }
        })
    }

    private fun setupCategoryFilters() {
        val categories = listOf(
            "All", 
            "Museums", 
            "Theme Parks", 
            "Parks & Nature", 
            "Zoos & Aquariums", 
            "Sports", 
            "Entertainment", 
            "Cultural Sites", 
            "Shopping", 
            "Landmarks"
        )
        
        categories.forEach { category ->
            val chipId = when (category) {
                "All" -> R.id.chipAll
                "Museums" -> R.id.chipMuseums
                "Theme Parks" -> R.id.chipThemeParks
                "Parks & Nature" -> R.id.chipParksNature
                "Zoos & Aquariums" -> R.id.chipZoosAquariums
                "Sports" -> R.id.chipSports
                "Entertainment" -> R.id.chipEntertainment
                "Cultural Sites" -> R.id.chipCulturalSites
                "Shopping" -> R.id.chipShopping
                "Landmarks" -> R.id.chipLandmarks
                else -> return@forEach
            }
            
            val chip = binding.categoryChipGroup.findViewById<Chip>(chipId)
            chip?.setOnClickListener {
                if (chip.isChecked) {
                    currentCategory = category
                    filterDestinations()
                    Log.d("AllDestinationsFragment", "Category filter applied: $category")
                }
            }
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            loadDestinations()
        }
    }

    private fun setupObservers() {
        // Observe top-rated places data
        placeViewModel.topRatedPlaces.observe(viewLifecycleOwner, Observer { topRatedPlaces ->
            allDestinations = topRatedPlaces
            filteredDestinations = allDestinations
            filterDestinations()
            showContentState()
            Log.d("AllDestinationsFragment", "Loaded ${allDestinations.size} destinations")
        })
        
        // Observe loading state
        placeViewModel.isLoadingTopRated.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        })
        
        // Observe errors
        placeViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                showErrorState(it)
                Log.e("AllDestinationsFragment", "Error loading destinations: $it")
            }
        })
    }

    private fun loadDestinations() {
        Log.d("AllDestinationsFragment", "Loading destinations...")
        placeViewModel.getTopRatedPlaces()
    }

    private fun filterDestinations() {
        filteredDestinations = allDestinations.filter { destination ->
            val matchesSearch = if (currentSearchQuery.isEmpty()) {
                true
            } else {
                destination.name.contains(currentSearchQuery, ignoreCase = true) ||
                destination.address?.contains(currentSearchQuery, ignoreCase = true) == true
            }

            val matchesCategory = if (currentCategory == "All") {
                true
            } else {
                // Use wayfareCategory field for filtering
                matchesWayfareCategory(destination, currentCategory)
            }

            matchesSearch && matchesCategory
        }

        destinationsAdapter.updateDestinations(filteredDestinations)
        
        if (filteredDestinations.isEmpty() && allDestinations.isNotEmpty()) {
            showEmptyState()
        } else if (filteredDestinations.isNotEmpty()) {
            showContentState()
        }

        Log.d("AllDestinationsFragment", 
            "Filtered: ${filteredDestinations.size}/${allDestinations.size} destinations " +
            "(search: '$currentSearchQuery', category: '$currentCategory')")
    }
    
    private fun matchesWayfareCategory(destination: TopRatedPlace, category: String): Boolean {
        return when (category) {
            "Museums" -> destination.wayfareCategory.contains("Museum", ignoreCase = true) ||
                        destination.wayfareCategory.contains("Art", ignoreCase = true)
            "Theme Parks" -> destination.wayfareCategory.contains("Theme", ignoreCase = true) ||
                            destination.wayfareCategory.contains("Amusement", ignoreCase = true)
            "Parks & Nature" -> destination.wayfareCategory.contains("Park", ignoreCase = true) ||
                               destination.wayfareCategory.contains("Nature", ignoreCase = true) ||
                               destination.wayfareCategory.contains("Garden", ignoreCase = true)
            "Zoos & Aquariums" -> destination.wayfareCategory.contains("Zoo", ignoreCase = true) ||
                                 destination.wayfareCategory.contains("Aquarium", ignoreCase = true)
            "Sports" -> destination.wayfareCategory.contains("Sport", ignoreCase = true) ||
                       destination.wayfareCategory.contains("Stadium", ignoreCase = true) ||
                       destination.wayfareCategory.contains("Recreation", ignoreCase = true)
            "Entertainment" -> destination.wayfareCategory.contains("Entertainment", ignoreCase = true) ||
                              destination.wayfareCategory.contains("Theater", ignoreCase = true) ||
                              destination.wayfareCategory.contains("Cinema", ignoreCase = true)
            "Cultural Sites" -> destination.wayfareCategory.contains("Cultural", ignoreCase = true) ||
                               destination.wayfareCategory.contains("Heritage", ignoreCase = true) ||
                               destination.wayfareCategory.contains("Historic", ignoreCase = true)
            "Shopping" -> destination.wayfareCategory.contains("Shopping", ignoreCase = true) ||
                         destination.wayfareCategory.contains("Market", ignoreCase = true) ||
                         destination.wayfareCategory.contains("Mall", ignoreCase = true)
            "Landmarks" -> destination.wayfareCategory.contains("Landmark", ignoreCase = true) ||
                          destination.wayfareCategory.contains("Monument", ignoreCase = true) ||
                          destination.wayfareCategory.contains("Memorial", ignoreCase = true)
            else -> false
        }
    }

    private fun toggleSearchVisibility() {
        if (binding.searchContainer.visibility == View.VISIBLE) {
            binding.searchContainer.visibility = View.GONE
            binding.searchEditText.text?.clear()
        } else {
            binding.searchContainer.visibility = View.VISIBLE
            binding.searchEditText.requestFocus()
        }
    }

    private fun onDestinationClicked(destination: TopRatedPlace) {
        Log.d("AllDestinationsFragment", "Destination clicked: ${destination.name}")
        
        // Navigate to destination details
        val fragment = DestinationDetailsFragment.newInstance(destination)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("DestinationDetails")
            .commit()
    }

    private fun showLoadingState() {
        binding.destinationsRecyclerView.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
    }

    private fun showContentState() {
        binding.loadingLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.destinationsRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.destinationsRecyclerView.visibility = View.GONE
        binding.loadingLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.VISIBLE
    }

    private fun showErrorState(message: String) {
        binding.destinationsRecyclerView.visibility = View.GONE
        binding.loadingLayout.visibility = View.GONE
        binding.emptyLayout.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorMessageText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}