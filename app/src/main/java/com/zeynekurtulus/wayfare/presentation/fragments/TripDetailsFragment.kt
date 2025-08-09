package com.zeynekurtulus.wayfare.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.zeynekurtulus.wayfare.R
import com.zeynekurtulus.wayfare.databinding.FragmentTripDetailsBinding
import com.zeynekurtulus.wayfare.domain.model.Route
import com.zeynekurtulus.wayfare.domain.model.RouteDetail
import com.zeynekurtulus.wayfare.presentation.adapters.TripDetailsMustVisitAdapter
import com.zeynekurtulus.wayfare.presentation.fragments.GiveFeedbackFragment
import com.zeynekurtulus.wayfare.presentation.fragments.ViewFeedbackFragment
import com.zeynekurtulus.wayfare.presentation.viewmodels.RouteListViewModel
import com.zeynekurtulus.wayfare.utils.getAppContainer
import com.zeynekurtulus.wayfare.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * TripDetailsFragment - Displays comprehensive trip information
 * 
 * Features:
 * - Trip overview with image, status, and key info
 * - Must-visit places horizontal list
 * - Day-by-day itinerary
 * - Share and menu actions
 */
class TripDetailsFragment : Fragment() {

    private var _binding: FragmentTripDetailsBinding? = null
    private val binding get() = _binding!!

    private val routeListViewModel: RouteListViewModel by viewModels {
        requireActivity().getAppContainer().viewModelFactory
    }

    private lateinit var mustVisitAdapter: TripDetailsMustVisitAdapter
    private var routeId: String? = null
    private var route: Route? = null

    companion object {
        private const val ARG_ROUTE_ID = "route_id"
        private const val ARG_ROUTE = "route"

        fun newInstance(routeId: String): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROUTE_ID, routeId)
                }
            }
        }

        fun newInstance(route: Route): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROUTE, route)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        loadTripData()
    }

    private fun setupUI() {
        Log.d("TripDetailsFragment", "setupUI called")
        setupToolbar()
        setupMustVisitPlaces()
    }

    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            Log.d("TripDetailsFragment", "Back button clicked")
            parentFragmentManager.popBackStack()
        }

        binding.shareButton.setOnClickListener {
            Log.d("TripDetailsFragment", "Share button clicked")
            shareTrip()
        }

        binding.menuButton.setOnClickListener {
            Log.d("TripDetailsFragment", "Menu button clicked")
            showTripMenu()
        }
        
        binding.giveFeedbackButton.setOnClickListener {
            navigateToGiveFeedback()
        }
        
        binding.viewFeedbackButton.setOnClickListener {
            navigateToViewFeedback()
        }
    }

    private fun setupMustVisitPlaces() {
        mustVisitAdapter = TripDetailsMustVisitAdapter { place ->
            // Handle must-visit place click
            showToast("Place details coming soon!")
        }

        binding.mustVisitRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mustVisitAdapter
        }
    }

    private fun setupObservers() {
        // For now, we'll only use the route data passed directly
        // Future: Add route detail fetching if needed
    }

    private fun loadTripData() {
        // Get data from arguments
        arguments?.let { args ->
            route = args.getParcelable(ARG_ROUTE)
            routeId = args.getString(ARG_ROUTE_ID)

            when {
                route != null -> {
                    // We have the route object, display it directly
                    displayTripFromRoute(route!!)
                }
                routeId != null -> {
                    // We have route ID, but for now we'll show a message
                    // Future: Implement route detail fetching
                    showToast("Route details fetching not yet implemented")
                    parentFragmentManager.popBackStack()
                }
                else -> {
                    Log.e("TripDetailsFragment", "No route data provided")
                    showToast("No trip data available")
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun displayTripFromRoute(route: Route) {
        binding.apply {
            // Trip title and destination
            tripTitleText.text = route.title
            destinationText.text = "${route.city}, ${route.country}"

            // Trip status
            setupTripStatus(route)

            // Privacy indicator
            setupPrivacyIndicator(route.isPublic)

            // Trip info cards
            datesText.text = formatDateRange(route.startDate, route.endDate)
            durationText.text = "${calculateDuration(route.startDate, route.endDate)} Days"
            budgetText.text = route.budget

            // Categories
            categoryChip.text = route.category
            travelStyleChip.text = route.travelStyle

            // Load trip image
            loadTripImage(route)

            // Must-visit places
            mustVisitAdapter.updatePlaces(route.mustVisit)

            // Itinerary
            setupItinerary(route.days)
        }
    }

    // Method removed - using displayTripFromRoute instead

    private fun setupTripStatus(route: Route) {
        val status = getTripStatus(route)
        val (statusText, backgroundColor, textColor) = when (status) {
            TripStatus.UPCOMING -> Triple(
                "Upcoming",
                R.color.primary_blue_50,
                R.color.primary_blue_700
            )
            TripStatus.ONGOING -> Triple(
                "Ongoing",
                R.color.secondary_green,
                R.color.white
            )
            TripStatus.COMPLETED -> Triple(
                "Completed",
                R.color.grey_100,
                R.color.text_secondary
            )
        }

        binding.statusChip.apply {
            text = statusText
            setChipBackgroundColorResource(backgroundColor)
            setTextColor(binding.root.context.getColor(textColor))
        }
    }

    private fun setupPrivacyIndicator(isPublic: Boolean) {
        binding.privacyIndicator.apply {
            if (isPublic) {
                setImageResource(R.drawable.ic_public)
                setColorFilter(binding.root.context.getColor(R.color.success_green))
            } else {
                setImageResource(R.drawable.ic_lock_private)
                setColorFilter(binding.root.context.getColor(R.color.text_hint))
            }
        }
    }

    private fun loadTripImage(route: Route) {
        val imageUrl = route.days.firstOrNull()?.activities?.firstOrNull()?.image
        loadImage(imageUrl)
    }

    // Method removed - using loadTripImage instead

    private fun loadImage(imageUrl: String?) {
        val requestOptions = RequestOptions()
            .transform(RoundedCorners(16))
            .placeholder(R.drawable.ic_map_placeholder)
            .error(R.drawable.ic_map_placeholder)

        Glide.with(this)
            .load(imageUrl)
            .apply(requestOptions)
            .into(binding.tripImageView)
    }

    private fun setupItinerary(days: List<com.zeynekurtulus.wayfare.domain.model.RouteDay>) {
        binding.itineraryContainer.removeAllViews()

        days.forEachIndexed { index, day ->
            val dayView = createDayView(index + 1, day)
            binding.itineraryContainer.addView(dayView)
        }
    }

    private fun createDayView(dayNumber: Int, day: com.zeynekurtulus.wayfare.domain.model.RouteDay): View {
        val inflater = LayoutInflater.from(requireContext())
        val dayView = inflater.inflate(R.layout.item_itinerary_day, binding.itineraryContainer, false)

        // Set day number and date
        dayView.findViewById<TextView>(R.id.dayNumberText).text = "Day $dayNumber"
        dayView.findViewById<TextView>(R.id.dayDateText).text = formatDate(day.date)

        // Set activities count
        val activitiesText = "${day.activities.size} activities"
        dayView.findViewById<TextView>(R.id.activitiesCountText).text = activitiesText

        // Set up activities container
        val activitiesContainer = dayView.findViewById<LinearLayout>(R.id.activitiesContainer)
        val expandIcon = dayView.findViewById<ImageView>(R.id.expandIcon)
        
        // Initially show only first 3 activities
        var isExpanded = false
        updateActivitiesDisplay(activitiesContainer, day.activities, isExpanded)
        
        // Set up expand/collapse functionality
        val clickableArea = dayView.findViewById<LinearLayout>(R.id.activitiesContainer).parent as LinearLayout
        clickableArea.setOnClickListener {
            isExpanded = !isExpanded
            updateActivitiesDisplay(activitiesContainer, day.activities, isExpanded)
            
            // Rotate expand icon
            val rotation = if (isExpanded) 180f else 0f
            expandIcon.animate().rotation(rotation).setDuration(200).start()
        }
        
        // Also make the expand icon clickable
        expandIcon.setOnClickListener {
            clickableArea.performClick()
        }

        return dayView
    }
    
    private fun updateActivitiesDisplay(container: LinearLayout, activities: List<com.zeynekurtulus.wayfare.domain.model.Activity>, showAll: Boolean) {
        container.removeAllViews()
        
        val activitiesToShow = if (showAll) activities else activities.take(3)
        activitiesToShow.forEach { activity ->
            val activityView = createActivityView(activity)
            container.addView(activityView)
        }
        
        // Add "show more" indicator if there are more activities and not expanded
        if (!showAll && activities.size > 3) {
            val moreIndicatorView = createMoreIndicatorView(activities.size - 3)
            container.addView(moreIndicatorView)
        }
    }
    
    private fun createMoreIndicatorView(moreCount: Int): View {
        val textView = TextView(requireContext())
        textView.text = "+$moreCount more activities"
        textView.setTextColor(resources.getColor(R.color.primary, null))
        textView.textSize = 12f
        textView.setPadding(16, 8, 16, 8)
        textView.gravity = android.view.Gravity.CENTER
        return textView
    }

    private fun createActivityView(activity: com.zeynekurtulus.wayfare.domain.model.Activity): View {
        val inflater = LayoutInflater.from(requireContext())
        val activityView = inflater.inflate(R.layout.item_activity_preview, binding.itineraryContainer, false)

        activityView.findViewById<TextView>(R.id.activityNameText).text = activity.placeName
        activityView.findViewById<TextView>(R.id.activityTimeText).text = activity.time

        return activityView
    }

    private fun formatDateRange(startDate: String, endDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

            val start = inputFormat.parse(startDate)
            val end = inputFormat.parse(endDate)

            if (start != null && end != null) {
                val startFormatted = outputFormat.format(start)
                val endFormatted = outputFormat.format(end)
                val year = yearFormat.format(end)

                "$startFormatted - $endFormatted $year"
            } else {
                "$startDate - $endDate"
            }
        } catch (e: Exception) {
            "$startDate - $endDate"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateDuration(startDate: String, endDate: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)

            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun getTripStatus(route: Route): TripStatus {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(route.startDate)
            val endDate = sdf.parse(route.endDate)
            val today = Calendar.getInstance().time

            when {
                startDate != null && endDate != null -> {
                    when {
                        today.before(startDate) -> TripStatus.UPCOMING
                        today.after(endDate) -> TripStatus.COMPLETED
                        else -> TripStatus.ONGOING
                    }
                }
                else -> TripStatus.UPCOMING
            }
        } catch (e: Exception) {
            TripStatus.UPCOMING
        }
    }

    private fun shareTrip() {
        route?.let { route ->
            val shareText = buildShareText(route)
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "My Trip to ${route.city}")
            }
            
            try {
                startActivity(Intent.createChooser(shareIntent, "Share Trip"))
            } catch (e: Exception) {
                showToast("Unable to share trip")
                Log.e("TripDetailsFragment", "Error sharing trip: ${e.message}")
            }
        }
    }
    
    private fun buildShareText(route: Route): String {
        val sb = StringBuilder()
        
        // Trip title and destination
        sb.append("🌍 ${route.title}\n")
        sb.append("📍 ${route.city}, ${route.country}\n\n")
        
        // Trip dates and duration
        val startDate = formatDate(route.startDate)
        val endDate = formatDate(route.endDate)
        val duration = "${calculateDuration(route.startDate, route.endDate)} Days"
        sb.append("📅 $startDate - $endDate\n")
        sb.append("⏱️ Duration: $duration\n")
        sb.append("💰 Budget: ${route.budget}\n")
        sb.append("🎯 Style: ${route.travelStyle}\n\n")
        
        // Must-visit places
        if (route.mustVisit.isNotEmpty()) {
            sb.append("🎯 Must-Visit Places:\n")
            route.mustVisit.take(3).forEach { place ->
                sb.append("• ${place.placeName}\n")
            }
            if (route.mustVisit.size > 3) {
                sb.append("• ...and ${route.mustVisit.size - 3} more places\n")
            }
            sb.append("\n")
        }
        
        // Day highlights
        if (route.days.isNotEmpty()) {
            sb.append("📋 Itinerary Highlights:\n")
            route.days.take(2).forEach { day ->
                val dayNumber = route.days.indexOf(day) + 1
                sb.append("Day $dayNumber (${formatDate(day.date)}):\n")
                day.activities.take(2).forEach { activity ->
                    sb.append("• ${activity.placeName}")
                    activity.time?.let { time -> sb.append(" at $time") }
                    sb.append("\n")
                }
                if (day.activities.size > 2) {
                    sb.append("• ...${day.activities.size - 2} more activities\n")
                }
                sb.append("\n")
            }
            if (route.days.size > 2) {
                sb.append("...and ${route.days.size - 2} more days of adventures!\n\n")
            }
        }
        
        sb.append("Created with Wayfare Travel Planner 🧳✈️")
        
        return sb.toString()
    }

    private fun showTripMenu() {
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.menuButton)
        popupMenu.menuInflater.inflate(R.menu.trip_details_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_trip -> {
                    showToast("Edit functionality coming soon!")
                    true
                }
                R.id.action_duplicate_trip -> {
                    duplicateTrip()
                    true
                }
                R.id.action_delete_trip -> {
                    showDeleteConfirmationDialog()
                    true
                }
                R.id.action_export_trip -> {
                    showToast("Export functionality coming soon!")
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    private fun duplicateTrip() {
        route?.let { currentRoute ->
            showToast("Creating a copy of ${currentRoute.title}...")
            // TODO: Implement trip duplication logic
            // This could navigate to TripMaker with pre-filled data
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        route?.let { currentRoute ->
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            
            // Create custom view for better styling
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_route, null)
            builder.setView(dialogView)
            
            val dialog = builder.create()
            
            // Set trip title in the dialog
            val tripTitleTextView = dialogView.findViewById<TextView>(R.id.tripTitleTextView)
            tripTitleTextView.text = currentRoute.title
            
            // Find buttons in custom layout
            val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
            val deleteButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.deleteButton)
            
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            
            deleteButton.setOnClickListener {
                dialog.dismiss()
                deleteTrip()
            }
            
            // Make dialog background white and dim the background
            dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_white)
            dialog.window?.setDimAmount(0.6f) // Dim the background
            
            dialog.show()
        }
    }
    
    private fun deleteTrip() {
        route?.routeId?.let { routeId ->
            routeListViewModel.deleteRoute(routeId)
            // Observe delete state if not already observing
            observeDeleteState()
        } ?: run {
            showToast("Cannot delete trip: Invalid trip ID")
        }
    }
    
    private fun observeDeleteState() {
        routeListViewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Loading -> {
                    // Show loading if needed
                }
                is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Success -> {
                    showToast("Trip deleted successfully")
                    // Navigate back to previous screen
                    parentFragmentManager.popBackStack()
                }
                is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Error -> {
                    showToast("Failed to delete trip: ${state.message}")
                }
                is com.zeynekurtulus.wayfare.presentation.viewmodels.DeleteRouteState.Idle -> {
                    // Do nothing
                }
            }
        }
    }
    
    private fun navigateToGiveFeedback() {
        val currentRoute = route ?: return
        Log.d("TripDetailsFragment", "Navigating to give feedback for route: ${currentRoute.title}")
        
        val fragment = GiveFeedbackFragment.newInstance(currentRoute)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("GiveFeedback")
            .commit()
    }
    
    private fun navigateToViewFeedback() {
        val currentRoute = route ?: return
        Log.d("TripDetailsFragment", "Navigating to view feedback for route: ${currentRoute.title}")
        
        val fragment = ViewFeedbackFragment.newInstance(currentRoute)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("ViewFeedback")
            .commit()
    }

    enum class TripStatus {
        UPCOMING, ONGOING, COMPLETED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}