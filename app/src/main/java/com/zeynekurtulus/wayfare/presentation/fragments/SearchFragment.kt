package com.zeynekurtulus.wayfare.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zeynekurtulus.wayfare.databinding.FragmentSearchBinding

/**
 * SearchFragment manages tabbed search interface for routes and places.
 * Features include:
 * - TabLayout with ViewPager2 for smooth navigation
 * - Two tabs: Search Routes and Search Places
 * - Swipe gesture support
 */
class SearchFragment : Fragment() {
    
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupTabs()
    }
    
    private fun setupViewPager() {
        val adapter = SearchPagerAdapter(this)
        binding.viewPager.adapter = adapter
    }
    
    private fun setupTabs() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Routes"
                1 -> "Places"
                else -> ""
            }
        }.attach()
    }
    
    override fun onPause() {
        super.onPause()
        // Reset search results when user navigates away from search screen
        resetSearchResults()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun resetSearchResults() {
        // Find and reset both search fragments
        val adapter = binding.viewPager.adapter as? SearchPagerAdapter
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is SearchRoutesFragment -> fragment.resetSearchResults()
                is SearchPlacesFragment -> fragment.resetSearchResults()
            }
        }
    }
    
    /**
     * ViewPager2 adapter for managing search tab fragments
     */
    private class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchRoutesFragment()
                1 -> SearchPlacesFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}