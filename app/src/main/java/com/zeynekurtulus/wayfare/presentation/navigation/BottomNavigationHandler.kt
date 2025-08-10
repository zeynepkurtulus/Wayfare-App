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
    // Note: Fragments will be recreated fresh on each tab switch to avoid ViewPager2 state restoration issues
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
        
        // Check for unsaved changes in Trip Maker before switching
        if (currentTab == NavigationTab.TRIP_MAKER && tripMakerFragment != null) {
            if (checkTripMakerUnsavedChanges(tab)) {
                return // User chose to stay, don't switch tabs
            }
        }
        
        // Check for unsaved changes in feedback screens
        if (checkFeedbackUnsavedChanges(tab)) {
            return // User chose to stay, don't switch tabs
        }
        
        // Clear SearchFragment reference before every tab switch to prevent ViewPager2 issues
        if (tab == NavigationTab.SEARCH) {
            searchFragment = null
        }
        
        val fragment = when (tab) {
            NavigationTab.HOME -> {
                if (homeFragment == null || homeFragment?.isDetached == true) {
                    homeFragment = HomeFragment()
                }
                homeFragment!!
            }
            NavigationTab.CALENDAR -> {
                if (calendarFragment == null || calendarFragment?.isDetached == true) {
                    calendarFragment = CalendarFragment()
                }
                calendarFragment!!
            }
            NavigationTab.SEARCH -> {
                android.util.Log.d("BottomNavigationHandler", "🔍 Creating/Getting SearchFragment")
                // ALWAYS create a new SearchFragment to completely avoid ViewPager2 state restoration issues
                android.util.Log.d("BottomNavigationHandler", "✨ Creating FRESH SearchFragment instance")
                searchFragment = SearchFragment()
                searchFragment!!
            }
            NavigationTab.TRIP_MAKER -> {
                if (tripMakerFragment == null || tripMakerFragment?.isDetached == true) {
                    tripMakerFragment = TripMakerFragment()
                }
                tripMakerFragment!!
            }
            NavigationTab.PROFILE -> {
                if (profileFragment == null || profileFragment?.isDetached == true) {
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
    
    /**
     * Check for unsaved changes in Trip Maker and show warning dialog
     * @param targetTab The tab user wants to switch to
     * @return true if user chose to stay (cancel navigation), false if proceed with navigation
     */
    private fun checkTripMakerUnsavedChanges(targetTab: NavigationTab): Boolean {
        val fragment = tripMakerFragment ?: return false
        
        // Check if fragment has a method to check unsaved changes
        try {
            val hasUnsavedChangesMethod = fragment.javaClass.getMethod("hasUnsavedChanges")
            val hasUnsavedChanges = hasUnsavedChangesMethod.invoke(fragment) as Boolean
            
            if (hasUnsavedChanges) {
                android.util.Log.d("BottomNavigationHandler", "⚠️ Unsaved changes detected in Trip Maker")
                showUnsavedChangesDialog(targetTab)
                return true
            }
        } catch (e: Exception) {
            android.util.Log.e("BottomNavigationHandler", "Error checking unsaved changes", e)
        }
        
        return false
    }
    
    /**
     * Show warning dialog for unsaved changes
     */
    private fun showUnsavedChangesDialog(targetTab: NavigationTab) {
        if (context !is androidx.appcompat.app.AppCompatActivity) return
        
        val activity = context as androidx.appcompat.app.AppCompatActivity
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        
        // Create custom view for better styling
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_unsaved_changes, null)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        
        // Find buttons in custom layout
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
        val continueButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.continueButton)
        
        cancelButton.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "🔄 User chose to stay in Trip Maker")
            dialog.dismiss()
            // User chose to stay, no further action needed
        }
        
        continueButton.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "🗑️ User chose to discard changes and switch tabs")
            dialog.dismiss()
            
            // Reset trip maker and proceed with navigation
            resetTripMaker()
            proceedWithTabSwitch(targetTab)
        }
        
        // Make dialog background white and dim the background
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        dialog.window?.setDimAmount(0.6f) // Dim the background
        
        dialog.show()
    }
    
    /**
     * Reset Trip Maker state when user confirms leaving with unsaved changes
     */
    private fun resetTripMaker() {
        tripMakerFragment?.let { fragment ->
            try {
                val resetMethod = fragment.javaClass.getMethod("resetTripMakerFromNavigation")
                resetMethod.invoke(fragment)
                android.util.Log.d("BottomNavigationHandler", "✅ Trip Maker reset successfully")
            } catch (e: Exception) {
                android.util.Log.e("BottomNavigationHandler", "Error resetting Trip Maker", e)
            }
        }
    }
    
    /**
     * Proceed with tab switch after handling unsaved changes
     */
    private fun proceedWithTabSwitch(targetTab: NavigationTab) {
        android.util.Log.d("BottomNavigationHandler", "🚀 Proceeding with tab switch to: $targetTab")
        
        val fragment = when (targetTab) {
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
                // This shouldn't happen since we're leaving Trip Maker
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
        val transaction = fragmentManager.beginTransaction()
            .replace(fragmentContainerId, fragment, targetTab.name)
        transaction.commit()
        
        // Update visual state
        updateTabSelection(targetTab)
        currentTab = targetTab
        android.util.Log.d("BottomNavigationHandler", "✅ Tab switch completed to: $targetTab")
    }
    
    /**
     * Check for unsaved changes in feedback screens
     * @param targetTab The tab user wants to switch to
     * @return true if user chose to stay (cancel navigation), false if proceed with navigation
     */
    private fun checkFeedbackUnsavedChanges(targetTab: NavigationTab): Boolean {
        // Check if there are any feedback fragments in the back stack
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            android.util.Log.d("BottomNavigationHandler", "Back stack entry: ${entry.name}")
            
            // Check if we have feedback fragments
            if (entry.name == "GiveFeedback" || entry.name == "GivePlaceFeedback") {
                android.util.Log.d("BottomNavigationHandler", "⚠️ Found feedback fragment in back stack")
                
                // Try to get the current fragment and check for unsaved changes
                val currentFragment = fragmentManager.findFragmentById(fragmentContainerId)
                if (currentFragment != null && hasFeedbackUnsavedChanges(currentFragment)) {
                    showFeedbackUnsavedChangesDialog(targetTab)
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Check if a feedback fragment has unsaved changes
     */
    private fun hasFeedbackUnsavedChanges(fragment: androidx.fragment.app.Fragment): Boolean {
        return try {
            when (fragment::class.java.simpleName) {
                "GiveFeedbackFragment", "GivePlaceFeedbackFragment" -> {
                    val hasUnsavedChangesMethod = fragment.javaClass.getMethod("hasUnsavedChanges")
                    hasUnsavedChangesMethod.invoke(fragment) as Boolean
                }
                else -> false
            }
        } catch (e: Exception) {
            android.util.Log.e("BottomNavigationHandler", "Error checking feedback unsaved changes", e)
            false
        }
    }
    
    /**
     * Show warning dialog for feedback unsaved changes
     */
    private fun showFeedbackUnsavedChangesDialog(targetTab: NavigationTab) {
        if (context !is androidx.appcompat.app.AppCompatActivity) return
        
        val activity = context as androidx.appcompat.app.AppCompatActivity
        val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
        
        // Create custom view for better styling
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_unsaved_changes, null)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        
        // Find buttons in custom layout
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
        val continueButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.continueButton)
        
        cancelButton.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "🔄 User chose to stay in feedback screen")
            dialog.dismiss()
        }
        
        continueButton.setOnClickListener {
            android.util.Log.d("BottomNavigationHandler", "🗑️ User chose to discard feedback and switch tabs")
            dialog.dismiss()
            
            // Pop feedback fragments from back stack and proceed
            while (fragmentManager.backStackEntryCount > 0) {
                val entry = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1)
                if (entry.name == "GiveFeedback" || entry.name == "GivePlaceFeedback") {
                    fragmentManager.popBackStack()
                } else {
                    break
                }
            }
            
            proceedWithTabSwitch(targetTab)
        }
        
        // Make dialog background white and dim the background
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
        dialog.window?.setDimAmount(0.6f) // Dim the background
        
        dialog.show()
    }
    
    /**
     * Clear all fragment references to prevent ViewPager2 state restoration issues
     * Call this when the activity is recreated or when fragments become invalid
     */
    fun clearFragmentReferences() {
        android.util.Log.d("BottomNavigationHandler", "🧹 Clearing all fragment references")
        homeFragment = null
        calendarFragment = null
        searchFragment = null
        tripMakerFragment = null
        profileFragment = null
    }
}