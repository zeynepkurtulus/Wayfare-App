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
            android.util.Log.d("BottomNavigationHandler", "🏠 HOME TAB CLICKED!")
            switchToTab(NavigationTab.HOME)
        }
        
        binding.calendarTab.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "📅 CALENDAR TAB CLICKED!")
            switchToTab(NavigationTab.CALENDAR)
        }
        
        // Add a more aggressive click listener approach for search tab
        binding.searchTab.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "🔍 SEARCH TAB CLICKED!")
            android.util.Log.d("BottomNavigationHandler", "Current tab before: $currentTab")
            switchToTab(NavigationTab.SEARCH)
        }
        
        // Also add click listener to the search tab's children to capture clicks
        binding.searchTab.setOnTouchListener { _, event ->
            android.util.Log.d("BottomNavigationHandler", "👆 SEARCH TAB TOUCHED! Action: ${event.action}")
            false // Don't consume the event
        }
        
        binding.tripMakerTab.setOnClickListener {
            switchToTab(NavigationTab.TRIP_MAKER)
        }
        
        binding.profileTab.setOnClickListener {
            switchToTab(NavigationTab.PROFILE)
        }
    }
    
    fun switchToTab(tab: NavigationTab) {
        android.util.Log.d("BottomNavigationHandler", "🚀 switchToTab called with: $tab")
        android.util.Log.d("BottomNavigationHandler", "Current tab: $currentTab")
        
        if (currentTab == tab) {
            android.util.Log.d("BottomNavigationHandler", "⚠️ Same tab selected, returning early")
            return
        }
        
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
                android.util.Log.d("BottomNavigationHandler", "🔍 Creating/Getting SearchFragment")
                if (searchFragment == null) {
                    android.util.Log.d("BottomNavigationHandler", "✨ Creating NEW SearchFragment instance")
                    searchFragment = SearchFragment()
                } else {
                    android.util.Log.d("BottomNavigationHandler", "♻️ Reusing existing SearchFragment")
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
        android.util.Log.d("BottomNavigationHandler", "📱 Starting fragment transaction for: $tab")
        android.util.Log.d("BottomNavigationHandler", "Fragment instance: ${fragment::class.java.simpleName}")
        android.util.Log.d("BottomNavigationHandler", "Container ID: $fragmentContainerId")
        
        val transaction = fragmentManager.beginTransaction()
            .replace(fragmentContainerId, fragment, tab.name)
        
        android.util.Log.d("BottomNavigationHandler", "🔄 Committing transaction...")
        transaction.commit()
        android.util.Log.d("BottomNavigationHandler", "✅ Transaction committed")
        
        // Update visual state
        android.util.Log.d("BottomNavigationHandler", "🎨 Updating tab selection...")
        updateTabSelection(tab)
        currentTab = tab
        android.util.Log.d("BottomNavigationHandler", "🏁 Tab switch complete! New current tab: $currentTab")
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