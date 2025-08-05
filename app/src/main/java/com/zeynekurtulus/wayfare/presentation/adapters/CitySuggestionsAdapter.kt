package com.zeynekurtulus.wayfare.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zeynekurtulus.wayfare.databinding.ItemCitySuggestionBinding
import com.zeynekurtulus.wayfare.domain.model.City

class CitySuggestionsAdapter(
    private val onCitySelected: (City) -> Unit
) : RecyclerView.Adapter<CitySuggestionsAdapter.CityViewHolder>() {
    
    private var cities = listOf<City>()
    
    fun updateCities(newCities: List<City>) {
        android.util.Log.d("CitySuggestionsAdapter", "Updating cities: ${newCities.size} items")
        newCities.forEachIndexed { index, city ->
            android.util.Log.d("CitySuggestionsAdapter", "City $index: ${city.displayText}")
        }
        cities = newCities
        notifyDataSetChanged()
        android.util.Log.d("CitySuggestionsAdapter", "notifyDataSetChanged called, itemCount: $itemCount")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCitySuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CityViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        android.util.Log.d("CitySuggestionsAdapter", "Binding city at position $position: ${cities[position].displayText}")
        holder.bind(cities[position])
    }
    
    override fun getItemCount(): Int = cities.size
    
    inner class CityViewHolder(
        private val binding: ItemCitySuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(city: City) {
            binding.apply {
                cityNameTextView.text = city.name
                countryTextView.text = city.country
                
                root.setOnClickListener {
                    onCitySelected(city)
                }
            }
        }
    }
}