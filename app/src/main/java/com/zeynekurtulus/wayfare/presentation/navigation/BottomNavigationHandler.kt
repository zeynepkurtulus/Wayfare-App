package com.zeynekurtulus.wayfare.presentation.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.LayoutBottomNavigationBinding
import com.zeynekurtulus.wayfare.presentation.fragments.CalendarFragment
import com.zeynekurtulus.wayfare.presentation.fragments.HomeFragment
import com.zeynekurtulus.wayfare.presentation.fragments.ProfileFragment
import com.zeynekurtulus.wayfare.presentation.fragments.SearchFragment
import com.zeynekurtulus.wayfare.presentation.fragments.TripMakerFragment

/**
 * BottomNavigationHandler - Manages bottom navigation state and fragment switching
 * 
 * This class encapsulates all bottom navigation logic:
 * - Fragment switching
 * - Visual state management (active/inactive tabs)
 * - Click handling
 * - Fragment lifecycle management
 */
class BottomNavigationHandler(
    private val context: Context,
    private val binding: LayoutBottomNavigationBinding,
    private val fragmentManager: FragmentManager,
    private val fragmentContainerId: Int
) {
    
    // Fragment instances (lazy initialization)
    private var homeFragment: HomeFragment? = null
    private var calendarFragment: CalendarFragment? = null
    private var searchFragment: SearchFragment? = null
    private var tripMakerFragment: TripMakerFragment? = null
    private var profileFragment: ProfileFragment? = null
    
    private var currentTab: NavigationTab? = null
    
    enum class NavigationTab {
        HOME, CALENDAR, SEARCH, TRIP_MAKER, PROFILE
    }
    
    init {
        setupClickListeners()
        // Start with home tab selected - force initial load
        switchToTab(NavigationTab.HOME)
    }
    
    private fun setupClickListeners() {
        binding.homeTab.setOnClickListener {
            switchToTab(NavigationTab.HOME)
        }
        
        binding.calendarTab.setOnClickListener {
            switchToTab(NavigationTab.CALENDAR)
        }
        
        binding.searchTab.setOnClickListener {
            switchToTab(NavigationTab.SEARCH)
        }
        
        binding.tripMakerTab.setOnClickListener {
            switchToTab(NavigationTab.TRIP_MAKER)
        }
        
        binding.profileTab.setOnClickListener {
            switchToTab(NavigationTab.PROFILE)
        }
    }
    
    fun switchToTab(tab: NavigationTab) {
        if (currentTab == tab) return
        
        val fragment = when (tab) {
            NavigationTab.HOME -> {
                if (homeFragment == null) {
                    homeFragment = HomeFragment()
                }
                homeFragment!!
            }
            NavigationTab.CALENDAR -> {
                if (calendarFragment == null) {
                    calendarFragment = CalendarFragment()
                }
                calendarFragment!!
            }
            NavigationTab.SEARCH -> {
                if (searchFragment == null) {
                    searchFragment = SearchFragment()
                }
                searchFragment!!
            }
            NavigationTab.TRIP_MAKER -> {
                if (tripMakerFragment == null) {
                    tripMakerFragment = TripMakerFragment()
                }
                tripMakerFragment!!
            }
            NavigationTab.PROFILE -> {
                if (profileFragment == null) {
                    profileFragment = ProfileFragment()
                }
                profileFragment!!
            }
        }
        
        // Switch fragment
        fragmentManager.beginTransaction()
            .replace(fragmentContainerId, fragment, tab.name)
            .commit()
        
        // Update visual state
        updateTabSelection(tab)
        currentTab = tab
    }
    
    private fun updateTabSelection(selectedTab: NavigationTab) {
        // Reset all tabs to inactive state
        resetAllTabs()
        
        // Highlight selected tab
        when (selectedTab) {
            NavigationTab.HOME -> {
                binding.homeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                binding.homeText.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            NavigationTab.CALENDAR -> {
                binding.calendarIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                binding.calendarText.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            NavigationTab.SEARCH -> {
                binding.searchText.setTextColor(ContextCompat.getColor(context, R.color.primary))
                // FAB stays blue always
            }
            NavigationTab.TRIP_MAKER -> {
                binding.tripMakerIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                binding.tripMakerText.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
            NavigationTab.PROFILE -> {
                binding.profileIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
                binding.profileText.setTextColor(ContextCompat.getColor(context, R.color.primary))
            }
        }
    }
    
    private fun resetAllTabs() {
        val inactiveIconColor = ContextCompat.getColor(context, R.color.nav_icon_inactive)
        val inactiveTextColor = ContextCompat.getColor(context, R.color.nav_text_inactive)
        
        binding.homeIcon.setColorFilter(inactiveIconColor)
        binding.homeText.setTextColor(inactiveTextColor)
        
        binding.calendarIcon.setColorFilter(inactiveIconColor)
        binding.calendarText.setTextColor(inactiveTextColor)
        
        binding.searchText.setTextColor(inactiveTextColor)
        
        binding.tripMakerIcon.setColorFilter(inactiveIconColor)
        binding.tripMakerText.setTextColor(inactiveTextColor)
        
        binding.profileIcon.setColorFilter(inactiveIconColor)
        binding.profileText.setTextColor(inactiveTextColor)
    }
    
    /**
     * Handle back button press
     * @return true if handled, false if should exit app
     */
    fun handleBackPress(): Boolean {
        // Check if there are fragments in the back stack first
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        
        // If no back stack, handle tab navigation
        return if (currentTab != NavigationTab.HOME) {
            // Navigate back to home
            switchToTab(NavigationTab.HOME)
            true
        } else {
            // Exit app
            false
        }
    }
    
    fun getCurrentTab(): NavigationTab = currentTab ?: NavigationTab.HOME
}